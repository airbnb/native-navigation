// A basic, minimalist EventEmitter
export default class EventEmitter {
  constructor() {
    this.registry = {};
  }

  on(event, handler) {
    if (!this.registry[event]) {
      this.registry[event] = [];
    }
    this.registry[event].push(handler);
    return { event, handler };
  }

  unsubscribe({ event, handler }) {
    this.off(event, handler);
  }

  off(event, handler) {
    const events = this.registry[event];
    if (!events) return;
    const index = events.indexOf(handler);
    if (index === -1) return;
    events.splice(index, 1);
    if (events.length === 0) {
      delete this.registry[event];
    }
  }

  emit(event, ...args) {
    const events = this.registry[event];
    if (!events) return;
    events.forEach(handler => handler(...args));
  }
}
