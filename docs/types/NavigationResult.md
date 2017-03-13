
# `NavigationResult` Type Definition

```js
type RESULT_OK = 1;
type RESULT_CANCELLED = 0;

type NavigationResult = {
  code: RESULT_OK | RESULT_CANCELLED;
  payload: Object;
}
```
