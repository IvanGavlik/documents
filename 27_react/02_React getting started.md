# React getting started

Load libraries into your html file

```html
<html>
  <body>
    <div id="app"></div>
    <script src="https://unpkg.com/react@18/umd/react.development.js"></script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
    <script>
      const app = document.getElementById('app');
      const root = ReactDOM.createRoot(app);
      root.render(<h1>Develop. Preview. Ship.</h1>);
    </script>
  </body>
</html>
```

`ReactDOM.createRoot()` target DOM element

`root.render(...)` render React code in the DOM

## JSX

Syntax extension for JavaScript

Browsers dont understand JSX so you'll need JavaScript compiler **Babel** ( add it as script to html file) 

Then script type is `test/jsx`

## Core concepts

Components

Props

State

### Components

Lego bricks

**Functions** inside script tag that **returns UI elements**

* should be capitalized 

* use with <> when calling

* TODO [importing and exporting](https://react.dev/learn/importing-and-exporting-components)

### Props

LIke html attibues to pass data ( information )

Read only info

It is object so you can use [Object descructing ]([Destructuring - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring))

To use it in html / view add `curly braces {}` (From JSX land into JavaScript )

### State

**Event** names are camelCase

Define a function to "handle" event before trigger / call

```js
function HomePage() {
  //     ...
  function handleClick() {
    console.log('increment like count');
  }

  return (
    <div>
      {/* ... */}
      <button onClick={handleClick}>Like</button>
    </div>
  );
}
```

**Hook** add additional logic ( state - information that changes over time ) to component

```js
function HomePage() {
  // ...
  const [likes, setLikes] = React.useState(0);

  function handleClick() {
    setLikes(likes + 1);
  }   

  return (
    // ...
    <button onClick={handleClick}>Like({likes})</button>
  );
}
```

likes - state variable

setLikes - fn that updates values

**Note:** Unlike props which are passed to components as the first function  parameter, the state is initiated and stored within a component. You can pass the state information to children components as props, but the  logic for updating the state should be kept within the component where  state was initially created.

TODO: 

https://nextjs.org/learn/react-foundations/updating-state

https://nextjs.org/learn/react-foundations/installation

https://react.dev/learn/creating-a-react-app
