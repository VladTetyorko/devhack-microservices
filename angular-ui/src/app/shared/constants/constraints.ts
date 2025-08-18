/**
 * Application-wide constants and constraints
 * Centralizes commonly used enums, options, and configuration values
 * to eliminate code duplication and ensure consistency across components.
 */

// Difficulty level options for questions
export interface DifficultyOption {
    value: string;
    label: string;
}

export const DIFFICULTY_LEVELS: DifficultyOption[] = [
    {value: 'Easy', label: 'Easy'},
    {value: 'Medium', label: 'Medium'},
    {value: 'Hard', label: 'Hard'}
];

// View mode options for list components
export enum ViewMode {
    TABLE = 'table',
    CARDS = 'cards',
    LIST = 'list'
}

export const VIEW_MODE_OPTIONS = [
    {value: ViewMode.TABLE, label: 'Table View'},
    {value: ViewMode.CARDS, label: 'Card View'},
    {value: ViewMode.LIST, label: 'List View'}
];

// Pagination defaults
export const PAGINATION_DEFAULTS = {
    PAGE: 0,
    SIZE: 10,
    DEFAULT_SORT: ['createdAt,desc']
};

// Sort options for questions and other entities
export const SORT_OPTIONS = {
    CREATED_DESC: ['createdAt,desc'],
    CREATED_ASC: ['createdAt,asc'],
    UPDATED_DESC: ['updatedAt,desc'],
    UPDATED_ASC: ['updatedAt,asc'],
    NAME_ASC: ['name,asc'],
    NAME_DESC: ['name,desc']
};

// Skeleton loading configuration
export const SKELETON_CONFIG = {
    DEFAULT_ITEMS_COUNT: 6,
    ITEMS: Array(6).fill(0)
};

// Question-specific constraints
export const QUESTION_CONSTRAINTS = {
    MIN_QUESTION_LENGTH: 10,
    MAX_QUESTION_LENGTH: 2000,
    MAX_SOURCE_LENGTH: 255,
    MAX_EXPECTED_ANSWER_LENGTH: 2000,
    MAX_HINTS_LENGTH: 500
};

// Search and filter defaults
export const SEARCH_DEFAULTS = {
    DEBOUNCE_TIME: 1000, // milliseconds
    MIN_SEARCH_LENGTH: 2
};

// WebSocket message types (if needed for centralization)
export enum WebSocketMessageType {
    QUESTION_GENERATED = 'QUESTION_GENERATED',
    ANSWER_EVALUATED = 'ANSWER_EVALUATED',
    GENERATION_PROGRESS = 'GENERATION_PROGRESS'
}

// Form validation messages
export const VALIDATION_MESSAGES = {
    REQUIRED: 'This field is required',
    MIN_LENGTH: (min: number) => `Minimum length is ${min} characters`,
    MAX_LENGTH: (max: number) => `Maximum length is ${max} characters`,
    INVALID_EMAIL: 'Please enter a valid email address',
    INVALID_FORMAT: 'Invalid format'
};

// API endpoints (if centralization is needed)
export const API_ENDPOINTS = {
    QUESTIONS: '/api/interview-questions',
    TAGS: '/api/tags',
    ANSWERS: '/api/answers',
    USERS: '/api/users'
};

// Export all constants as a single object for convenience
export const AppConstraints = {
    DIFFICULTY_LEVELS,
    VIEW_MODE_OPTIONS,
    PAGINATION_DEFAULTS,
    SORT_OPTIONS,
    SKELETON_CONFIG,
    QUESTION_CONSTRAINTS,
    SEARCH_DEFAULTS,
    VALIDATION_MESSAGES,
    API_ENDPOINTS
} as const;