import '@testing-library/jest-dom/vitest';

// Polyfill ResizeObserver for jsdom (required by cmdk / Radix components)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(globalThis as any).ResizeObserver = class ResizeObserver {
    observe() { }
    unobserve() { }
    disconnect() { }
};

// Polyfill Element.scrollIntoView for jsdom (required by cmdk)
Element.prototype.scrollIntoView = function () { };
