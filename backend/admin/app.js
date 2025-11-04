// Configuration
const API_BASE_URL = 'https://jetpack.dolphincoder.com/api/admin';
const API_URL = 'https://jetpack.dolphincoder.com/api';

// State
let currentAdmin = null;
let chapters = [];
let topics = [];
let notes = [];
let quizzes = [];
let questions = [];
let quillEditor = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupLoginForm();
    setupNavigation();
    setupLogout();
    checkSession();
});

// Check if admin is already logged in
function checkSession() {
    // For simplicity, we'll rely on server-side sessions
    // You can enhance this with localStorage tokens
}

// Login Form
function setupLoginForm() {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        await login(username, password);
    });
}

async function login(username, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/login.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (data.success) {
            currentAdmin = data.data.admin;
            document.getElementById('adminName').textContent = currentAdmin.full_name;
            document.getElementById('loginScreen').classList.remove('active');
            document.getElementById('adminPanel').classList.add('active');
            loadDashboard();
        } else {
            showLoginError(data.message);
        }
    } catch (error) {
        showLoginError('Login failed. Please try again.');
    }
}

function showLoginError(message) {
    const errorDiv = document.getElementById('loginError');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

// Logout
function setupLogout() {
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await fetch(`${API_BASE_URL}/logout.php`);
        location.reload();
    });
}

// Navigation
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const section = item.dataset.section;
            switchSection(section);
        });
    });
}

function switchSection(section) {
    // Update nav
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-section="${section}"]`).classList.add('active');

    // Update content
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.getElementById(`${section}Section`).classList.add('active');

    // Update title
    const titles = {
        dashboard: 'Dashboard',
        chapters: 'Chapters',
        topics: 'Topics',
        notes: 'Notes',
        quizzes: 'Quizzes',
        questions: 'Questions',
        users: 'Users'
    };
    document.getElementById('pageTitle').textContent = titles[section];

    // Load data
    switch(section) {
        case 'dashboard': loadDashboard(); break;
        case 'chapters': loadChapters(); break;
        case 'topics': loadTopics(); break;
        case 'notes': loadNotes(); break;
        case 'quizzes': loadQuizzes(); break;
        case 'questions': loadQuestions(); break;
    }
}

// Dashboard
async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard.php`);
        const data = await response.json();

        if (data.success) {
            const stats = data.data;
            document.getElementById('statUsers').textContent = stats.total_users;
            document.getElementById('statChapters').textContent = stats.total_chapters;
            document.getElementById('statTopics').textContent = stats.total_topics;
            document.getElementById('statQuizzes').textContent = stats.total_quizzes;
            document.getElementById('statQuestions').textContent = stats.total_questions;
            document.getElementById('statSessions').textContent = stats.total_sessions;

            // Recent users
            const tbody = document.getElementById('recentUsersTable');
            tbody.innerHTML = stats.recent_users.map(user => `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.email || 'N/A'}</td>
                    <td>${user.display_name || 'N/A'}</td>
                    <td>${new Date(user.created_at).toLocaleDateString()}</td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load dashboard:', error);
    }
}

// Chapters
async function loadChapters() {
    try {
        const response = await fetch(`${API_BASE_URL}/chapters.php`);
        const data = await response.json();

        if (data.success) {
            chapters = data.data;
            renderChapters();
        }
    } catch (error) {
        console.error('Failed to load chapters:', error);
    }
}

function renderChapters() {
    const tbody = document.getElementById('chaptersTable');
    tbody.innerHTML = chapters.map(chapter => `
        <tr>
            <td>${chapter.id}</td>
            <td>${chapter.title}</td>
            <td>${chapter.description || ''}</td>
            <td>${chapter.topic_count}</td>
            <td>${chapter.order_index}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editChapter(${chapter.id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteChapter(${chapter.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function showAddChapterModal() {
    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h2>Add Chapter</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="chapterForm">
                        <div class="form-group">
                            <label>Title *</label>
                            <input type="text" id="chapterTitle" required>
                        </div>
                        <div class="form-group">
                            <label>Description</label>
                            <textarea id="chapterDescription"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="chapterOrder" value="0">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="saveChapter()">Save</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;
}

async function saveChapter() {
    const title = document.getElementById('chapterTitle').value;
    const description = document.getElementById('chapterDescription').value;
    const order_index = document.getElementById('chapterOrder').value;

    try {
        const response = await fetch(`${API_BASE_URL}/chapters.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, description, order_index })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadChapters();
            alert('Chapter added successfully!');
        } else {
            alert('Failed to add chapter: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function deleteChapter(id) {
    if (!confirm('Are you sure you want to delete this chapter? This will also delete all its topics!')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/chapters.php`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        const data = await response.json();
        if (data.success) {
            loadChapters();
            alert('Chapter deleted successfully!');
        } else {
            alert('Failed to delete chapter: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Topics
async function loadTopics() {
    try {
        const response = await fetch(`${API_BASE_URL}/topics.php`);
        const data = await response.json();

        if (data.success) {
            topics = data.data;
            renderTopics();
        }
    } catch (error) {
        console.error('Failed to load topics:', error);
    }
}

function renderTopics() {
    const tbody = document.getElementById('topicsTable');
    tbody.innerHTML = topics.map(topic => `
        <tr>
            <td>${topic.id}</td>
            <td>${topic.chapter_title}</td>
            <td>${topic.title}</td>
            <td>${topic.description || ''}</td>
            <td>${topic.order_index}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editTopic(${topic.id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteTopic(${topic.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

async function showAddTopicModal() {
    // Load chapters for dropdown
    if (chapters.length === 0) {
        await loadChapters();
    }

    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h2>Add Topic</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="topicForm">
                        <div class="form-group">
                            <label>Chapter *</label>
                            <select id="topicChapter" required>
                                <option value="">Select Chapter</option>
                                ${chapters.map(ch => `<option value="${ch.id}">${ch.title}</option>`).join('')}
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Title *</label>
                            <input type="text" id="topicTitle" required>
                        </div>
                        <div class="form-group">
                            <label>Description</label>
                            <textarea id="topicDescription"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Content</label>
                            <textarea id="topicContent" style="min-height: 200px;"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="topicOrder" value="0">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="saveTopic()">Save</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;
}

async function saveTopic() {
    const chapter_id = document.getElementById('topicChapter').value;
    const title = document.getElementById('topicTitle').value;
    const description = document.getElementById('topicDescription').value;
    const content = document.getElementById('topicContent').value;
    const order_index = document.getElementById('topicOrder').value;

    try {
        const response = await fetch(`${API_BASE_URL}/topics.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ chapter_id, title, description, content, order_index })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadTopics();
            alert('Topic added successfully!');
        } else {
            alert('Failed to add topic: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function deleteTopic(id) {
    if (!confirm('Are you sure you want to delete this topic?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/topics.php`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        const data = await response.json();
        if (data.success) {
            loadTopics();
            alert('Topic deleted successfully!');
        } else {
            alert('Failed to delete topic: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Quizzes
async function loadQuizzes() {
    try {
        const response = await fetch(`${API_BASE_URL}/quizzes.php`);
        const data = await response.json();

        if (data.success) {
            quizzes = data.data;
            renderQuizzes();
        }
    } catch (error) {
        console.error('Failed to load quizzes:', error);
    }
}

function renderQuizzes() {
    const tbody = document.getElementById('quizzesTable');
    tbody.innerHTML = quizzes.map(quiz => `
        <tr>
            <td>${quiz.id}</td>
            <td>${quiz.title}</td>
            <td>${quiz.description || ''}</td>
            <td>${quiz.question_count}</td>
            <td>${quiz.order_index}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editQuiz(${quiz.id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteQuiz(${quiz.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function showAddQuizModal() {
    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h2>Add Quiz</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="quizForm">
                        <div class="form-group">
                            <label>Title *</label>
                            <input type="text" id="quizTitle" required>
                        </div>
                        <div class="form-group">
                            <label>Description</label>
                            <textarea id="quizDescription"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="quizOrder" value="0">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="saveQuiz()">Save</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;
}

async function saveQuiz() {
    const title = document.getElementById('quizTitle').value;
    const description = document.getElementById('quizDescription').value;
    const order_index = document.getElementById('quizOrder').value;

    try {
        const response = await fetch(`${API_BASE_URL}/quizzes.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, description, order_index })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadQuizzes();
            alert('Quiz added successfully!');
        } else {
            alert('Failed to add quiz: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function deleteQuiz(id) {
    if (!confirm('Are you sure you want to delete this quiz? This will also delete all its questions!')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/quizzes.php`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        const data = await response.json();
        if (data.success) {
            loadQuizzes();
            alert('Quiz deleted successfully!');
        } else {
            alert('Failed to delete quiz: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Notes Section
async function loadNotes() {
    try {
        const response = await fetch(`${API_BASE_URL}/notes.php`);
        const data = await response.json();

        if (data.success) {
            notes = data.data;
            renderNotes();
        }
    } catch (error) {
        console.error('Failed to load notes:', error);
    }
}

function renderNotes() {
    const tbody = document.getElementById('notesTable');
    tbody.innerHTML = notes.map(note => {
        const contentPreview = note.content ?
            note.content.replace(/<[^>]*>/g, '').substring(0, 50) + '...' :
            'No content yet';

        return `
        <tr>
            <td>${note.id}</td>
            <td>${note.topic_title} <small style="color: #666;">(${note.chapter_title})</small></td>
            <td>${note.title}</td>
            <td><span class="content-preview">${contentPreview}</span></td>
            <td>${note.order_index}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editNote(${note.id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteNote(${note.id})">Delete</button>
            </td>
        </tr>
    `;}).join('');
}

async function showAddNoteModal() {
    // Load topics for dropdown (and their chapters)
    if (topics.length === 0) {
        await loadTopics();
    }

    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()" style="max-width: 900px;">
                <div class="modal-header">
                    <h2>Add Note</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="noteForm">
                        <div class="form-group">
                            <label>Topic *</label>
                            <select id="noteTopic" required>
                                <option value="">Select Topic</option>
                                ${topics.map(t => `<option value="${t.id}">${t.chapter_title} → ${t.title}</option>`).join('')}
                            </select>
                            <small class="help-text">Choose which topic this note belongs to</small>
                        </div>
                        <div class="form-group">
                            <label>Note Title *</label>
                            <input type="text" id="noteTitle" required placeholder="e.g., Introduction to Composables - Lesson Content">
                        </div>
                        <div class="form-group">
                            <label>Content (Rich Text Editor)</label>
                            <div id="quillEditor" style="height: 300px; background: white;"></div>
                            <input type="hidden" id="noteContent">
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="noteOrder" value="0">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="saveNote()">Save Note</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;

    // Initialize Quill editor
    setTimeout(() => {
        if (typeof Quill !== 'undefined') {
            quillEditor = new Quill('#quillEditor', {
                theme: 'snow',
                placeholder: 'Write your lesson content here...',
                modules: {
                    toolbar: [
                        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
                        [{ 'font': [] }],
                        [{ 'size': ['small', false, 'large', 'huge'] }],
                        ['bold', 'italic', 'underline', 'strike'],
                        [{ 'color': [] }, { 'background': [] }],
                        [{ 'script': 'sub'}, { 'script': 'super' }],
                        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                        [{ 'indent': '-1'}, { 'indent': '+1' }],
                        [{ 'align': [] }],
                        ['blockquote', 'code-block'],
                        ['link', 'image'],
                        ['clean']
                    ]
                }
            });
        } else {
            console.error('Quill editor not loaded');
        }
    }, 100);
}

async function saveNote() {
    const topic_id = document.getElementById('noteTopic').value;
    const title = document.getElementById('noteTitle').value;
    const order_index = document.getElementById('noteOrder').value;
    const content = quillEditor ? quillEditor.root.innerHTML : '';

    if (!topic_id || !title) {
        alert('Please fill in all required fields');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/notes.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ topic_id, title, content, order_index })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadNotes();
            alert('Note created successfully!');
        } else {
            alert('Failed to add note: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function editNote(id) {
    const note = notes.find(n => n.id === id);
    if (!note) return;

    if (topics.length === 0) {
        await loadTopics();
    }

    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()" style="max-width: 900px;">
                <div class="modal-header">
                    <h2>Edit Note</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="noteForm">
                        <div class="form-group">
                            <label>Topic</label>
                            <input type="text" value="${note.topic_title}" disabled>
                            <small class="help-text">Topic cannot be changed after creation</small>
                            <input type="hidden" id="noteId" value="${note.id}">
                        </div>
                        <div class="form-group">
                            <label>Note Title *</label>
                            <input type="text" id="noteTitle" value="${note.title}" required>
                        </div>
                        <div class="form-group">
                            <label>Content (Rich Text Editor)</label>
                            <div id="quillEditor" style="height: 300px; background: white;"></div>
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="noteOrder" value="${note.order_index}">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="updateNote()">Update Note</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;

    setTimeout(() => {
        if (typeof Quill !== 'undefined') {
            quillEditor = new Quill('#quillEditor', {
                theme: 'snow',
                modules: {
                    toolbar: [
                        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
                        [{ 'font': [] }],
                        [{ 'size': ['small', false, 'large', 'huge'] }],
                        ['bold', 'italic', 'underline', 'strike'],
                        [{ 'color': [] }, { 'background': [] }],
                        [{ 'script': 'sub'}, { 'script': 'super' }],
                        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                        [{ 'indent': '-1'}, { 'indent': '+1' }],
                        [{ 'align': [] }],
                        ['blockquote', 'code-block'],
                        ['link', 'image'],
                        ['clean']
                    ]
                }
            });
            quillEditor.root.innerHTML = note.content || '';
        } else {
            console.error('Quill editor not loaded');
        }
    }, 100);
}

async function updateNote() {
    const id = document.getElementById('noteId').value;
    const title = document.getElementById('noteTitle').value;
    const order_index = document.getElementById('noteOrder').value;
    const content = quillEditor ? quillEditor.root.innerHTML : '';

    if (!title) {
        alert('Please enter a title');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/notes.php`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, title, content, order_index })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadNotes();
            alert('Note updated successfully!');
        } else {
            alert('Failed to update note: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function deleteNote(id) {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/notes.php`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        const data = await response.json();
        if (data.success) {
            loadNotes();
            alert('Note deleted successfully!');
        } else {
            alert('Failed to delete note: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Questions
async function loadQuestions() {
    try {
        const response = await fetch(`${API_BASE_URL}/questions.php`);
        const data = await response.json();

        if (data.success) {
            questions = data.data;
            renderQuestions();
        }
    } catch (error) {
        console.error('Failed to load questions:', error);
    }
}

function renderQuestions() {
    const tbody = document.getElementById('questionsTable');
    tbody.innerHTML = questions.map(q => {
        const options = [q.option_a, q.option_b, q.option_c, q.option_d];
        const correctAnswer = options[q.correct_answer_index];

        return `
            <tr>
                <td>${q.id}</td>
                <td>${q.quiz_title}</td>
                <td>${q.question_text.substring(0, 50)}...</td>
                <td>${correctAnswer}</td>
                <td>${q.order_index}</td>
                <td>
                    <button class="btn btn-sm btn-warning" onclick="editQuestion(${q.id})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteQuestion(${q.id})">Delete</button>
                </td>
            </tr>
        `;
    }).join('');
}

async function showAddQuestionModal() {
    // Load quizzes for dropdown
    if (quizzes.length === 0) {
        await loadQuizzes();
    }

    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h2>Add Question</h2>
                    <button class="modal-close" onclick="closeModal()">&times;</button>
                </div>
                <div class="modal-body">
                    <form id="questionForm">
                        <div class="form-group">
                            <label>Quiz *</label>
                            <select id="questionQuiz" required>
                                <option value="">Select Quiz</option>
                                ${quizzes.map(qz => `<option value="${qz.id}">${qz.title}</option>`).join('')}
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Question *</label>
                            <textarea id="questionText" required></textarea>
                        </div>
                        <div class="form-group">
                            <label>Option A *</label>
                            <input type="text" id="questionOptionA" required>
                        </div>
                        <div class="form-group">
                            <label>Option B *</label>
                            <input type="text" id="questionOptionB" required>
                        </div>
                        <div class="form-group">
                            <label>Option C *</label>
                            <input type="text" id="questionOptionC" required>
                        </div>
                        <div class="form-group">
                            <label>Option D *</label>
                            <input type="text" id="questionOptionD" required>
                        </div>
                        <div class="form-group">
                            <label>Correct Answer *</label>
                            <select id="questionCorrectAnswer" required>
                                <option value="0">A</option>
                                <option value="1">B</option>
                                <option value="2">C</option>
                                <option value="3">D</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Explanation</label>
                            <textarea id="questionExplanation"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Order</label>
                            <input type="number" id="questionOrder" value="0">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                    <button class="btn btn-primary" onclick="saveQuestion()">Save</button>
                </div>
            </div>
        </div>
    `;
    document.getElementById('modalContainer').innerHTML = modal;
}

async function saveQuestion() {
    const quiz_id = document.getElementById('questionQuiz').value;
    const question_text = document.getElementById('questionText').value;
    const option_a = document.getElementById('questionOptionA').value;
    const option_b = document.getElementById('questionOptionB').value;
    const option_c = document.getElementById('questionOptionC').value;
    const option_d = document.getElementById('questionOptionD').value;
    const correct_answer_index = parseInt(document.getElementById('questionCorrectAnswer').value);
    const explanation = document.getElementById('questionExplanation').value;
    const order_index = document.getElementById('questionOrder').value;

    try {
        const response = await fetch(`${API_BASE_URL}/questions.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                quiz_id, question_text, option_a, option_b, option_c, option_d,
                correct_answer_index, explanation, order_index
            })
        });

        const data = await response.json();
        if (data.success) {
            closeModal();
            loadQuestions();
            alert('Question added successfully!');
        } else {
            alert('Failed to add question: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function deleteQuestion(id) {
    if (!confirm('Are you sure you want to delete this question?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/questions.php`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });

        const data = await response.json();
        if (data.success) {
            loadQuestions();
            alert('Question deleted successfully!');
        } else {
            alert('Failed to delete question: ' + data.message);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Edit functions (stub - you can implement these similarly)
function editChapter(id) {
    alert('Edit chapter functionality - implement similarly to add');
}

function editTopic(id) {
    alert('Edit topic functionality - implement similarly to add');
}

function editQuiz(id) {
    alert('Edit quiz functionality - implement similarly to add');
}

function editQuestion(id) {
    alert('Edit question functionality - implement similarly to add');
}

// Modal helper
function closeModal(event) {
    if (!event || event.target.classList.contains('modal-overlay')) {
        document.getElementById('modalContainer').innerHTML = '';
    }
}
