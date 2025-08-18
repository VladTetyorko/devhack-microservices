import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';

// WebSocket dependencies
import {Client, IMessage} from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';

// Environment and services
import {environment} from '../../../environments/environment';
import {AuthService} from '../basic/auth.service';

/**
 * Base interface for WebSocket messages.
 * All specific WebSocket message types should extend this interface.
 */
export interface BaseWebSocketMessage {
    type: string;
    data?: any;
    timestamp: number;
}

/**
 * Configuration interface for WebSocket connections.
 */
export interface WebSocketConfig {
    endpoint: string;
    reconnectInterval?: number;
    maxReconnectAttempts?: number;
    heartbeatIncoming?: number;
    heartbeatOutgoing?: number;
    debug?: boolean;
}

/**
 * Abstract base service for WebSocket connections.
 *
 * This service provides common WebSocket functionality including:
 * - Connection management with automatic reconnection
 * - JWT token-based authentication
 * - Environment-based configuration
 * - Error handling and logging
 * - Connection status monitoring
 *
 * Child services should extend this class and implement the abstract methods
 * to handle specific message types and subscriptions.
 *
 * Follows SOLID principles:
 * - Single Responsibility: Handles only WebSocket connection concerns
 * - Open/Closed: Open for extension via inheritance, closed for modification
 * - Liskov Substitution: Child services can replace this base service
 * - Interface Segregation: Separates connection logic from business logic
 * - Dependency Inversion: Depends on AuthService abstraction
 */
@Injectable()
export abstract class BaseWebSocketService<T extends BaseWebSocketMessage> {
    protected stompClient: Client | null = null;
    protected connectionStatus = new BehaviorSubject<boolean>(false);
    protected messageSubject = new Subject<T>();
    protected reconnectAttempts = 0;
    protected maxReconnectAttempts: number;
    protected reconnectInterval: number;
    protected config: WebSocketConfig;
    protected isSubscribed = false;

    /**
     * Constructor for BaseWebSocketService.
     *
     * @param authService - Service for handling authentication and token management
     * @param config - WebSocket configuration (optional, uses environment defaults)
     */
    constructor(
        protected authService: AuthService,
        config?: Partial<WebSocketConfig>
    ) {
        // Initialize configuration with environment defaults
        this.config = {
            endpoint: environment.wsUrl,
            reconnectInterval: environment.webSocket.reconnectInterval,
            maxReconnectAttempts: environment.webSocket.maxReconnectAttempts,
            heartbeatIncoming: environment.webSocket.heartbeatIncoming,
            heartbeatOutgoing: environment.webSocket.heartbeatOutgoing,
            debug: environment.webSocket.debug,
            ...config
        };

        this.maxReconnectAttempts = this.config.maxReconnectAttempts!;
        this.reconnectInterval = this.config.reconnectInterval!;

        // Don't auto-connect in constructor to avoid initialization issues
        // Connection will be established lazily when first needed
    }

    /**
     * Get connection status as observable.
     *
     * @returns Observable<boolean> - Connection status stream
     */
    getConnectionStatus(): Observable<boolean> {
        return this.connectionStatus.asObservable();
    }

    /**
     * Get messages as observable and establish WebSocket connection if needed.
     *
     * This method implements lazy connection - the WebSocket connection is only
     * established when this method is first called. This prevents unnecessary
     * connections during service initialization.
     *
     * @returns Observable stream of WebSocket messages
     */
    getMessages(): Observable<T> {
        // Establish connection lazily when first needed
        if (!this.stompClient) {
            this.connect();
        }
        return this.messageSubject.asObservable();
    }

    /**
     * Check if WebSocket is connected.
     *
     * @returns boolean - Current connection status
     */
    isConnected(): boolean {
        return this.connectionStatus.value;
    }

    /**
     * Disconnect from WebSocket server.
     */
    disconnect(): void {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.connectionStatus.next(false);
        }
        this.isSubscribed = false;
        console.log('[DEBUG_LOG] WebSocket disconnected');
    }

    /**
     * Send a message to the WebSocket server.
     *
     * @param destination - The destination endpoint
     * @param body - The message body
     * @param headers - Optional headers
     */
    protected sendMessage(destination: string, body: any, headers?: { [key: string]: string }): void {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({
                destination,
                body: JSON.stringify(body),
                headers
            });
        } else {
            console.warn('[DEBUG_LOG] Cannot send message: WebSocket not connected');
        }
    }

    /**
     * Connect to WebSocket server.
     * This method handles the common connection logic including authorization.
     */
    protected connect(): void {
        console.log('[DEBUG_LOG] Attempting to connect to WebSocket...');

        // Get current authentication token
        const token = this.authService.getToken();

        // Prepare connection headers with authorization if token exists
        const connectHeaders: { [key: string]: string } = {};
        if (token) {
            connectHeaders['Authorization'] = `Bearer ${token}`;
            console.log('[DEBUG_LOG] Adding authorization header to WebSocket connection Bearer ${token}' + token);
        }

        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(environment.wsUrl),
            connectHeaders: {Authorization: `Bearer ${token}`},
            reconnectDelay: this.reconnectInterval,
            heartbeatIncoming: this.config.heartbeatIncoming!,
            heartbeatOutgoing: this.config.heartbeatOutgoing!,
            debug: this.config.debug ? (s) => console.log('[DEBUG_LOG] STOMP:', s) : undefined
        });


        this.stompClient.onConnect = (frame) => {
            console.log('[DEBUG_LOG] Connected to WebSocket successfully');
            this.connectionStatus.next(true);
            this.reconnectAttempts = 0;
            this.onConnected(frame);
        };

        this.stompClient.onStompError = (frame) => {
            const errorMessage = frame.headers['message'] || 'Unknown STOMP error';
            const errorDetails = frame.body || 'No additional details';

            console.error('[DEBUG_LOG] WebSocket STOMP error:', errorMessage);
            console.error('[DEBUG_LOG] Error details:', errorDetails);

            // Check if it's an authentication error
            if (errorMessage.toLowerCase().includes('unauthorized') ||
                errorMessage.toLowerCase().includes('authentication') ||
                errorMessage.toLowerCase().includes('forbidden')) {
                console.error('[DEBUG_LOG] Authentication error detected. Token may be invalid or expired.');
                this.onAuthenticationError(errorMessage, errorDetails);
            }

            this.connectionStatus.next(false);
            this.onError(errorMessage, errorDetails);
            this.handleReconnect();
        };

        this.stompClient.onWebSocketClose = (event) => {
            console.log('[DEBUG_LOG] WebSocket connection closed. Code:', event.code, 'Reason:', event.reason);
            this.connectionStatus.next(false);
            this.onConnectionClosed(event);
            this.handleReconnect();
        };

        this.stompClient.onWebSocketError = (event) => {
            console.error('[DEBUG_LOG] WebSocket connection error:', event);
            this.connectionStatus.next(false);
            this.onConnectionError(event);
        };

        this.stompClient.activate();
    }

    /**
     * Handle reconnection logic.
     */
    protected handleReconnect(): void {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`[DEBUG_LOG] Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        } else {
            console.error('[DEBUG_LOG] Max reconnection attempts reached. Please refresh the page.');
            this.onMaxReconnectAttemptsReached();
        }
    }

    /**
     * Parse incoming WebSocket message.
     *
     * @param message - Raw WebSocket message
     * @returns Parsed message or null if parsing fails
     */
    protected parseMessage(message: IMessage): T | null {
        try {
            const parsedMessage: T = JSON.parse(message.body);
            console.log('[DEBUG_LOG] Received WebSocket message:', parsedMessage);
            return parsedMessage;
        } catch (error) {
            console.error('[DEBUG_LOG] Error parsing WebSocket message:', error);
            return null;
        }
    }

    // Abstract methods that child classes must implement

    /**
     * Called when WebSocket connection is established.
     * Child classes should implement this to set up their specific subscriptions.
     *
     * @param frame - Connection frame
     */
    protected abstract onConnected(frame: any): void;

    /**
     * Called when a WebSocket error occurs.
     * Child classes can override this to handle specific error scenarios.
     *
     * @param errorMessage - Error message
     * @param errorDetails - Additional error details
     */
    protected onError(errorMessage: string, errorDetails: string): void {
        // Default implementation - child classes can override
    }

    /**
     * Called when an authentication error occurs.
     * Child classes can override this to handle authentication-specific logic.
     *
     * @param errorMessage - Error message
     * @param errorDetails - Additional error details
     */
    protected onAuthenticationError(errorMessage: string, errorDetails: string): void {
        // Default implementation - child classes can override
    }

    /**
     * Called when WebSocket connection is closed.
     * Child classes can override this to handle connection closure.
     *
     * @param event - Close event
     */
    protected onConnectionClosed(event: CloseEvent): void {
        // Default implementation - child classes can override
    }

    /**
     * Called when a WebSocket connection error occurs.
     * Child classes can override this to handle connection errors.
     *
     * @param event - Error event
     */
    protected onConnectionError(event: Event): void {
        // Default implementation - child classes can override
    }

    /**
     * Called when maximum reconnection attempts are reached.
     * Child classes can override this to handle this scenario.
     */
    protected onMaxReconnectAttemptsReached(): void {
        // Default implementation - child classes can override
    }
}