# # From React to Next.js

Uses file-system routing

Add `export default` to component to mark component as the main component of the page 

export  - share code from one flie to other

default - export one main thing

## Server and Client Components

**Network Boundary** separates client (browser) and server components. Ex: fetch and render data on the server comonent, render interactive components (Buttons) on the client

Componenets are split into **two module graphs**: server and client module graph

After Server components are rendered **React Server Component Payload (RSC)** is sent to the client 

- rendered result of server components

- placeholder for Client components and references to their JS    

Server Components are by default to make a Client Component set `use client` at the top of the file

**Server Components**

- render on the server (Node.js)

- HTML sent to browser

- No JS sent to client

- no interactivity

- what can do:
  
  - fecth data (asynx / await)
  
  - access DB
  
  - read files on the server
  
  - use secret API keys (never exposed to client)
  
  - reduc JS size
  
  - Better SEO (contenr rendered on server)

**Client Compnents**

- render in the browser

- JS sent to the client

- what can do
  
  - hooks (useState, useEffect ..)
  
  - handle user interactions (onClick, onChange)
  
  - browser APIs (window, localStorage)

Client Components "infect" children: Once you add 'use client', all imported components become client components too.  Solution is to pass Server components as props to Client components

**Decission Tree**: Ask do you need interactivity ( clicks, inputs, etc. ) ?

* yes then it is Client component

* no then it is Server componet

### Common Patterns

**Basic**

* start with Server components use Client only when necessary

* client componet push down the tree (at leaf level)

* separate interactive parts from static parts

* fetch data on server

* client componets smalll and focused

* for forms and data mutations user server actions instead of clinet-side API calls

* parallel data fetching on server

* avoid prop drilling with context

* Env variable
  
  * Server components can use any env variable
  
  * Client components an use only NEXT_PUBLIC_ variables

* Many third-party libraries use browser APIs and must be client components.
  
  * Lazy load chart 

* Testing
  
  * Server components: Test with Node env
  
  * Client components: Test with browser env

**Mix Server and client components**

Import Client component into Server component 

In the Server component use 

* async await to fetch data 

* populate data 

* use imported climent component to have interactivity (buttons, clicks ...)

**Form with Validation** 

Server component returns Client component (whic is interactiv form)

Stack

+ React Hook FOrm (form state)

+ Zod (validation)

+ Server Actions (submission)

+ Tailwind CSS (styling)

**Dashboard with Data and Interactivity**

Server component as wrapper and used to fetch data contains other server component that display data and other client componets that have some interactiviy like chart

**Loading and Error States**

For Server components use loading.js erros.js

For Client components use state

TODO

**Share logic with custom hooks** 

TODO example (Client components)

**Dialogs** - TODO how to implement it 

**Autocomplete components** TODO how to implement it best options / practices

**Infinite scroll / load more**

## Client Components "infect" Children

When you mark component with `use client` eveything it imports also becomes a client component, even if those files don't have it - infections spread doen the tree

When you need to mix server and client componet use compsition (passing children)

```js
  // app/page.js (Server Component)
  import ClientWrapper from '@/components/ClientWrapper';
  import ServerContent from '@/components/ServerContent';

  export default function Page() {
    return (
      <ClientWrapper>
        {/* Pass as children - stays server component! ✅ */}
        <ServerContent />
      </ClientWrapper>
    );
  }

  // components/ClientWrapper.js
  'use client';

  import { useState } from 'react';

  export default function ClientWrapper({ children }) {
    const [isOpen, setIsOpen] = useState(false);

    return (
      <div>
        <button onClick={() => setIsOpen(!isOpen)}>Toggle</button>
        {isOpen && children} {/* children stays server component! */}
      </div>
    );
  }

  // components/ServerContent.js
  // No 'use client' - stays server component ✅

  export default async function ServerContent() {
    const data = await fetch('https://api.example.com/data');
    return <div>{/* Render data */}</div>;
  }
```

Why this works

* ClientWrapper doesn't import ServerContent

* ServerContent is passed as children from a Server Component

## Server actions

Asynchronous fn that tun on the server but can be called directly from Client Components use it for mutations (create, update, delete)

* RPC (Remote Procedure Calls) made easy - call a fn on the client it runs on the server

* API endpoint you don't have to create manually

* fn that can be called directly from forms

Create

* separate file with `user server` at top

* inline in server component

**Can be called**

* forms

* event handlers

* timers/intercals

* drag and drop

* send email in background

* batch operations

* from any client code

Do not use for

* just reading data (use async Server componetns)

* pure client side logic

* real time features (use WebSovkets)

**Form specific**

When form is submited Server Actions automatically receive a FormData object

Use `useFormState` hook for better UX with loading states and error messages

To update chached data use `revalidatePath` or `revalidateTag`  

To redirect use `redirect(...)`

TODO DO i CALL db DIRECTRLY OR CALL API ENDPOINTS

**Data fetching**

* designed for mutations, not data fecthing

* no build-in HTTP caching

* not RESTful 

* sends more data than necessary

## Server Components with Suspense

TODO [React Foundations: Next Steps | Next.js](https://nextjs.org/learn/react-foundations/next-steps)
