
# `NavigationResult` Type Definition

```js
type RESULT_OK = -1;
type RESULT_CANCELED = 0;

type NavigationResult = {
  code: RESULT_OK | RESULT_CANCELED;
  payload: Object;
}
```
