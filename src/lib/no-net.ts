// Runtime fail-closed: blocks all network APIs in production builds
declare const global: any;

const block = (name: string) => () => {
  throw new Error(`Network disabled by Spark offline policy: ${name}`);
};

export function installNetworkGuards() {
  if (__DEV__) return; // allow Metro/devtools in dev
  try {
    if (typeof global.fetch === "function") global.fetch = block("fetch");
    if (typeof global.XMLHttpRequest !== "undefined")
      global.XMLHttpRequest = class extends (global.XMLHttpRequest as any) {
        constructor() {
          super();
          throw new Error("Network disabled by Spark offline policy: XHR");
        }
      };
    if (typeof global.WebSocket !== "undefined")
      global.WebSocket = class {
        constructor() {
          throw new Error("Network disabled by Spark offline policy: WebSocket");
        }
      } as any;
  } catch {
    // no-op fallback
  }
}
