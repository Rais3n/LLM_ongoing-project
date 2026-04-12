# Multi-Agent LLM Assistant

A Java-based assistant that routes user requests between specialized agents, summarizes unread emails with an LLM, extracts task candidates from those emails, and manages tasks through Google Tasks.

## Overview

This project explores a multi-agent pattern in a practical workflow:

- An email-focused agent reads unread emails and generates concise summaries.
- A task-focused agent interprets user intent and performs task actions.
- A router decides which agent should handle the request.
- The system can suggest tasks from emails and add selected items to Google Tasks.

The goal of the project is to combine LLM orchestration with real external integrations instead of building a toy chat app.

## Features

- CLI-based assistant loop in Java
- Multi-agent request routing
- Email summarization from unread Gmail messages
- LLM-based task extraction from email content
- Task operations through Google Tasks
- Prompt-driven tool calling
- Embedding-based routing with Qdrant, plus a fallback router when embeddings are unavailable

## Architecture

The code is organized into focused layers:

- `orchestration`  
  Contains the top-level coordinator and request router.
- `service`  
  Contains application logic for AI calls, email summarization, and task handling.
- `infrastructure`  
  Contains external integrations such as email access.
- `aiTools`  
  Contains task-related operations performed against Google Tasks.
- `model` and `dto`  
  Contain the project data structures.
- `prompts`  
  Contains prompt templates used for summarization, task filtering, and tool calling.

## Tech Stack

- Java
- Maven
- OkHttp
- org.json
- JavaMail
- Jsoup
- Google Tasks API
- Hugging Face Inference Router
- Qdrant

## Example Flow

1. User sends a message in the CLI.
2. The router selects either the email agent or the task agent.
3. If the email agent is selected:
   - unread emails are fetched from Gmail
   - the emails are summarized with the LLM
   - task candidates are extracted
   - the user can choose which tasks to add
4. If the task agent is selected:
   - the LLM decides which task operation to call
   - the app adds, updates, deletes, or lists tasks in Google Tasks

## Setup

### Requirements

- Java 22
- Maven
- Gmail account with an app password
- Google Cloud credentials for Google Tasks API
- Hugging Face token
- Optional: local Qdrant instance for embedding-based routing

### Environment Variables

Set these environment variables before running the app:

- `HF_TOKEN`
- `EMAIL_USERNAME`
- `GMAIL_APP_PASSWORD`

### Google Credentials

Place your Google API OAuth credentials file in the project root as:

- `credentials.json`

## Running

Build and run with Maven:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="pl.clockworkjava.Main"
```

If you do not run Qdrant or embeddings are unavailable, the app will fall back to a simple keyword-based router.

## What I Focused On

This project was built to explore:

- multi-agent system design in Java
- safe handling of unreliable LLM outputs
- integrating LLMs with real tools and APIs
- routing requests between specialized assistants
- turning unstructured email content into actionable tasks


> Built a Java multi-agent LLM assistant that summarizes unread emails, extracts actionable tasks, routes requests between specialized agents, and syncs selected tasks with Google Tasks.
