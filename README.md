<h1 align="center">ResumeWriter</h1>
<h3 align="center">Custom Resume Writing Made Easy</h3>

<p align="center">
  Job hunting is hard work, and customizing a resume for each job listing can be tedious. 
  <strong>ResumeWriter</strong> is a web app designed to simplify this process by automatically tailoring resumes to specific job listings.
</p>

<h2>Features</h2>
<ul>
  <li><strong>Web-Based</strong>: Accessible from any device with a browser.</li>
  <li><strong>User Registration</strong>: Users can create accounts and manage their profiles.</li>
  <li><strong>Resume Upload</strong>: Upload existing resumes, skills, work history, and education.</li>
  <li><strong>Job Listing Integration</strong>: Copy and paste job listings for analysis.</li>
  <li><strong>Custom Resume Generation</strong>: Automatically generates resumes in a user-chosen format.</li>
</ul>

<h2>Tech Stack</h2>
<h3>Frontend</h3>
<ul>
  <li><strong>HTML</strong>: Structure of the web pages.</li>
  <li><strong>Tailwind CSS</strong>: Utility-first CSS framework for styling.</li>
  <li><strong>React.js</strong>: JavaScript library for building the user interface.</li>
</ul>

<h3>Backend</h3>
<ul>
  <li><strong>REST API</strong>: Built using <strong>Spring Boot</strong> (Java).</li>
  <li><strong>Database</strong>: <strong>MySQL</strong> for storing user data, resumes, and job listings.</li>
  <li><strong>Authentication</strong>: <strong>Spring Security</strong> for user registration and login.</li>
</ul>

<h3>Testing</h3>
<ul>
  <li><strong>Jest</strong>: For unit testing the frontend (React components).</li>
  <li><strong>JUnit</strong>: For unit testing the backend (Spring Boot).</li>
</ul>

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
    <strong><code>server/</code></strong>: Backend (Spring Boot)
    <ul>
      <li><code>src/main/java/com/resumewriter/</code>
        <ul>
          <li><code>controller/</code>: API controllers (e.g., <code>ResumeController</code>)</li>
          <li><code>service/</code>: Business logic (e.g., resume generation)</li>
          <li><code>repository/</code>: Database interactions (e.g., <code>UserRepository</code>)</li>
          <li><code>model/</code>: Database entities (e.g., <code>User</code>, <code>Resume</code>)</li>
          <li><code>config/</code>: Configuration files (e.g., security, database)</li>
        </ul>
      </li>
      <li><code>src/main/resources/</code>
        <ul>
          <li><code>application.properties</code>: Configuration (e.g., MySQL connection)</li>
          <li><code>static/</code>: Serve static files (optional)</li>
        </ul>
      </li>
      <li><code>pom.xml</code>: Maven dependencies</li>
    </ul>
  </li>
  <li>
    <strong>Root Files</strong>
    <ul>
      <li><code>.env</code>: Environment variables (e.g., database credentials)</li>
      <li><code>.gitignore</code>: Files and folders to ignore in Git</li>
      <li><code>README.md</code>: Project documentation</li>
    </ul>
  </li>
</ul>

<h3>Why This Structure?</h3>
<ul>
  <li><strong>Separation of Concerns</strong>: Frontend and backend code are kept in separate folders (<code>client</code> and <code>server</code>).</li>
  <li><strong>Ease of Maintenance</strong>: All related files (e.g., React components, API routes) are grouped logically.</li>
  <li><strong>Shared Configurations</strong>: Root-level files like <code>.env</code> can be shared across the project.</li>
</ul>
