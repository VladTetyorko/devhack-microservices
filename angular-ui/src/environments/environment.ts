// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
    production: false,

    // API Configuration
    apiUrl: 'http://localhost:8081/api',

    // WebSocket Configuration
    wsUrl: '/ws',

    // Authentication Configuration
    authTokenKey: 'devhack_auth_token',

    // WebSocket Settings
    webSocket: {
        reconnectInterval: 5000,
        maxReconnectAttempts: 5,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: true
    },

    // Application Settings
    app: {
        name: 'DevHack',
        version: '1.0.0'
    }
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.