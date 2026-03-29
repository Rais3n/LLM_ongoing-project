# Multi-Agent LLM Assistant
## Project Description
This project implements a multi-agent conversational AI system using LLMs. The system currently supports two specialized agents: one for email summarization and one for task management. The architecture is designed to allow future expansion for tool calling and other agent functions.  

---

## Current Agents

### 1. EmailSummarizer
- **Role:** Fetches recent emails and provides concise summaries.  
- **Functionality:**  
  - Fetches recent emails directly from the email server using JavaMail (javax.mail) and parses content for summarization.
  - Summarize key information.  
  - Suggest potential tasks to add to the task manager.  

### 2. TaskManager
- **Role:** Manages user tasks in Google Calendar.  
- **Functionality:**  
  - Add, update, delete, or fetch tasks.  
  - Ask the user if they want to add tasks from email summaries.  

---
