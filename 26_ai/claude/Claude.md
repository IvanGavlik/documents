# Claude

## Tips for beginners
* lets explore first
    * analyze
    * give me overview
    * Start with broad questions, then narrow down to specific areas dive deeper into specific components
    * find relevant code
    * get context 
    * understand the execution flow (be specific)
* be specific with your request
* use step by step instructions
    * if needed give command or description how to reproduce state/bug

## Common workflows

Create custom subagents for your workflow /agents Claude will automatically delegate but you also can explicitly request. TODO check documentation https://code.claude.com/docs/en/sub-agents

Use Plan Mode when you have multi step implementation, when you want to explore codebase 
when you want to iterate on the direction 
claude --premission-mode plan
> I need to refactor our authentication system to use OAuth2. Create a detailed migration plan.

Ask for tests 

Create PR

Handle documentation

Reference files user @ to include files or directories 

Extended thinking - provide context and ask Claude to think (think more, think longer)
https://docs.claude.com/en/docs/build-with-claude/prompt-engineering/extended-thinking-tips

Create custom slash commands  https://code.claude.com/docs/en/slash-commands
they can be project specific - you can pass arguments


## Settings

https://code.claude.com/docs/en/settings#available-settings


stao https://code.claude.com/docs/en/sub-agents
