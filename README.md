# SmartLibrary — Library Management Website

A clean, responsive digital library interface for managing books, copy availability, and borrower records. The website contains only library-management features.

## Features
- Add books with title, author, category, year, ISBN, and number of copies
- Search and filter the collection
- See total copies, available copies, and borrowed copies
- Record borrower name, contact information, and due date
- Return borrowed copies and automatically restore availability
- Edit borrower details and remove books
- Save the collection in the current browser using local storage

## Run in VS Code
1. Open this project folder in VS Code.
2. Open a terminal and run:

```bash
cd frontend
npm install
npm run dev
```

3. Open the local address shown in the terminal, usually `http://localhost:5173`.

## Storage note
The collection is saved in the browser's local storage. It is not shared between different browsers or devices.

- Task Planner: add/remove tasks, set durations and deadlines, set available hours, and generate a recommended schedule with resource estimates and conflict explanations
