<h1 align="center">ResumeWriter</h1> <h3 align="center">Custom Resume Writing Made Easy</h3><p align="center"> Job hunting is hard work, and customizing a resume for each job listing can be tedious. <strong>ResumeWriter</strong> is a web app designed to simplify this process by automatically tailoring resumes to specific job listings. </p><h2>Features</h2> <ul> <li><strong>Web-Based</strong>: Accessible from any device with a browser.</li> <li><strong>User Registration</strong>: Users can create accounts and manage their profiles.</li> <li><strong>Resume Upload</strong>: Upload existing resumes, skills, work history, and education.</li> <li><strong>Job Listing Integration</strong>: Copy and paste job listings for analysis.</li> <li><strong>Custom Resume Generation</strong>: Automatically generates resumes in a user-chosen format.</li> </ul><h2>Tech Stack</h2> <h3>Frontend</h3> <ul> <li><strong>HTML</strong>: Structure of the web pages.</li> <li><strong>Tailwind CSS</strong>: Utility-first CSS framework for styling.</li> <li><strong>React.js</strong>: JavaScript library for building the user interface.</li> </ul><h3>Backend</h3> <ul> <li><strong>REST API</strong>: Built using <strong>Node.js</strong> and <strong>Express.js</strong>.</li> <li><strong>Database</strong>: <strong>PostgreSQL</strong> for storing user data, resumes, and job listings.</li> <li><strong>Authentication</strong>: <strong>Firebase</strong> for user registration and login.</li> </ul><h3>Testing</h3> <ul> <li><strong>Jest</strong>: For unit testing the frontend (React components).</li> <li><strong>Supertest</strong>: For testing the backend (API endpoints).</li> </ul>
<h2>Mono-Repo Structure</h2>
<p>This project uses a <strong>mono-repo structure</strong> to organize the frontend and backend code in a single repository. Here's how the files are structured:</p>

<h3>Root Directory</h3>
<ul>
  <li>
    <strong><code>client/</code></strong>: Frontend (React.js)
    <ul>
      <li><code>public/</code>: Static assets (e.g., <code>index.html</code>)</li>
      <li>
        <code>src/</code>: React components, pages, and logic
        <ul>
          <li><code>components/</code>: Reusable UI components</li>
          <li><code>pages/</code>: Page components (e.g., Home, Dashboard)</li>
          <li><code>App.js</code>: Main application component</li>
          <li><code>index.js</code>: Entry point for the React app</li>
        </ul>
      </li>
      <li><code>package.json</code>: Frontend dependencies</li>
      <li><code>tailwind.config.js</code>: Tailwind CSS configuration</li>
    </ul>
  </li>
  <li>
    <strong><code>server/</code></strong>: Backend (Node.js + Express.js)
    <ul>
      <li><code>controllers/</code>: Logic for handling API requests</li>
      <li><code>models/</code>: Database models (e.g., User, Resume)</li>
      <li><code>routes/</code>: API routes (e.g., <code>/api/resumes</code>)</li>
      <li><code>middleware/</code>: Custom middleware (e.g., authentication)</li>
      <li><code>config/</code>: Configuration files (e.g., database setup)</li>
      <li><code>app.js</code>: Main Express application</li>
      <li><code>server.js</code>: Entry point for the backend server</li>
      <li><code>package.json</code>: Backend dependencies</li>
    </ul>
  </li>
  <li>
    <strong>Root Files</strong>
    <ul>
      <li><code>.env</code>: Environment variables (e.g., database credentials)</li>
      <li><code>.gitignore</code>: Files and folders to ignore in Git</li>
      <li><code>package.json</code>: Root-level dependencies and scripts</li>
      <li><code>README.md</code>: Project documentation</li>
    </ul>
  </li>
</ul>

<h3>Why This Structure?</h3>
<ul>
  <li><strong>Separation of Concerns</strong>: Frontend and backend code are kept in separate folders (<code>client</code> and <code>server</code>).</li>
  <li><strong>Ease of Maintenance</strong>: All related files (e.g., React components, API routes) are grouped logically.</li>
  <li><strong>Shared Configurations</strong>: Root-level files like <code>.env</code> and <code>package.json</code> can be shared across the project.</li>
</ul>
