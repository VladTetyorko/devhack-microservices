import {Injectable} from '@angular/core';
import {IMessage} from '@stomp/stompjs';

// Base WebSocket service
import {BaseWebSocketMessage, BaseWebSocketService} from './base-websocket.service';
import {AuthService} from '../basic/auth.service';

/**
 * Interface for question-specific WebSocket messages.
 * Extends the base WebSocket message interface.
 */
export interface QuestionWebSocketMessage extends BaseWebSocketMessage {
    type: 'QUESTION_CREATED' | 'QUESTION_UPDATED' | 'QUESTION_DELETED';
    data: any;
}

/**
 * Service for handling WebSocket connections and real-time question updates.
 *
 * This service extends BaseWebSocketService to provide question-specific
 * WebSocket functionality. It handles real-time updates for interview questions
 * (create, update, delete operations) while inheriting common WebSocket
 * functionality from the parent class.
 *
 * Features:
 * - Question-specific message handling and routing
 * - Subscription to /topic/questions endpoint
 * - Duplicate subscription prevention using subscription flag
 * - Ping/refresh functionality for questions
 * - Inherits: authentication, reconnection, error handling from base class
 *
 * Subscription Management:
 * - Uses isSubscribed flag to prevent duplicate subscriptions
 * - Automatically subscribes on connection establishment
 * - Skips subscription if already subscribed (prevents duplicates on reconnection)
 * - Resets subscription flag on disconnect
 *
 * Follows SOLID principles:
 * - Single Responsibility: Handles only question-specific WebSocket logic
 * - Open/Closed: Extends base functionality without modifying it
 * - Liskov Substitution: Can be used wherever BaseWebSocketService is expected
 * - Interface Segregation: Separates question logic from general WebSocket logic
 * - Dependency Inversion: Depends on AuthService abstraction
 */
@Injectable({
    providedIn: 'root'
})
export class QuestionWebSocketService extends BaseWebSocketService<QuestionWebSocketMessage> {

    /**
     * Constructor for QuestionWebSocketService.
     *
     * @param authService - Service for handling authentication and token management
     */
    constructor(authService: AuthService) {
        // Call parent constructor with default configuration
        super(authService);
    }

    /**
     * Implementation of abstract method from BaseWebSocketService.
     * Called when WebSocket connection is established.
     * Sets up question-specific subscriptions.
     *
     * @param frame - Connection frame from STOMP
     */
    protected onConnected(frame: any): void {
        console.log('[DEBUG_LOG] QuestionWebSocketService connected, setting up subscriptions');
        this.subscribeToQuestions();
    }

    /**
     * Subscribe to question updates.
     * Checks if already subscribed to prevent duplicate subscriptions.
     */
    private subscribeToQuestions(): void {
        if (this.stompClient && this.stompClient.connected && !this.isSubscribed) {
            console.log('[DEBUG_LOG] Creating new subscription to /topic/questions');
            this.stompClient.subscribe('/topic/questions', (message: IMessage) => {
                try {
                    const questionMessage: QuestionWebSocketMessage = JSON.parse(message.body);
                    console.log('[DEBUG_LOG] Received WebSocket message:', questionMessage);
                    this.messageSubject.next(questionMessage);
                } catch (error) {
                    console.error('[DEBUG_LOG] Error parsing WebSocket message:', error);
                }
            });
            this.isSubscribed = true;
            console.log('[DEBUG_LOG] Successfully subscribed to /topic/questions');
        } else if (this.isSubscribed) {
            console.log('[DEBUG_LOG] Already subscribed to /topic/questions, skipping duplicate subscription');
        }
    }


    /**
     * Send a ping message to keep connection alive.
     */
    sendPing(): void {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({
                destination: '/devhack/questions/ping',
                body: JSON.stringify({timestamp: Date.now()})
            });
        }
    }

    /**
     * Request a refresh of the question list.
     */
    requestRefresh(): void {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({
                destination: '/devhack/questions/refresh',
                body: JSON.stringify({timestamp: Date.now()})
            });
        }
    }

    /**
     * Disconnect from WebSocket server.
     */
    override disconnect(): void {
        super.disconnect();
    }

    /**
     * Check if WebSocket is connected.
     */
    override isConnected(): boolean {
        return this.connectionStatus.value;
    }
}