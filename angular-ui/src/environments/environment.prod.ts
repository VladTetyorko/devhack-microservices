export const environment = {
    production: true,

    // API Configuration - Update these URLs for production deployment
    apiUrl: '/api',

    // WebSocket Configuration - Update for production deployment
    wsUrl: '/ws',

    // Authentication Configuration
    authTokenKey: 'devhack_auth_token',

    // WebSocket Settings
    webSocket: {
        reconnectInterval: 5000,
        maxReconnectAttempts: 5,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: false
    },

    // Application Settings
    app: {
        name: 'DevHack',
        version: '1.0.0'
    }
};