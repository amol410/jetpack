// ============================================
// UPDATED FUNCTIONS FOR APP.JS
// Replace the corresponding functions in app.js with these
// ============================================

// Global variable to hold Quill editor instance
let quillEditor = null;

// ==== TOPICS FUNCTIONS (WITH RICH TEXT EDITOR) ====

async function showAddTopicModal() {
    // Load chapters for dropdown
    if (chapters.length === 0) {
        await loadChapters();
    }

    const modal = `
        <div class="modal-overlay" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()" style="max-width: 900px;">
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
                            <textarea id="topicDescription" rows="2"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Content (Rich Text Editor)</label>
                            <div id="quillEditor" style="height: 300px; background: white;"></div>
                            <input type="hidden" id="topicContent">
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

    // Initialize Quill editor after modal is rendered
    setTimeout(() => {
        quillEditor = new Quill('#quillEditor', {
            theme: 'snow',
            placeholder: 'Write your topic content here...',
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
    }, 100);
}

async function saveTopic() {
    const chapter_id = document.getElementById('topicChapter').value;
    const title = document.getElementById('topicTitle').value;
    const description = document.getElementById('topicDescription').value;
    const order_index = document.getElementById('topicOrder').value;

    // Get HTML content from Quill editor
    const content = quillEditor ? quillEditor.root.innerHTML : '';

    if (!chapter_id || !title) {
        alert('Please fill in all required fields');
        return;
    }

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

// ==== QUIZZES FUNCTIONS (WITH CHAPTER/TOPIC SELECTION) ====

function renderQuizzes() {
    const tbody = document.getElementById('quizzesTable');
    tbody.innerHTML = quizzes.map(quiz => {
        let linkInfo = '';
        if (quiz.topic_title) {
            linkInfo = `<span class="badge badge-topic">${quiz.chapter_title} → ${quiz.topic_title}</span>`;
        } else if (quiz.chapter_title) {
            linkInfo = `<span class="badge badge-chapter">${quiz.chapter_title}</span>`;
        } else {
            linkInfo = '<span class="badge badge-general">General</span>';
        }

        return `
        <tr>
            <td>${quiz.id}</td>
            <td>${quiz.title}</td>
            <td>${quiz.description || ''}</td>
            <td>${linkInfo}</td>
            <td>${quiz.question_count}</td>
            <td>${quiz.order_index}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editQuiz(${quiz.id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteQuiz(${quiz.id})">Delete</button>
            </td>
        </tr>
    `}).join('');
}

async function showAddQuizModal() {
    // Load chapters for dropdown
    if (chapters.length === 0) {
        await loadChapters();
    }
    // Load topics for dropdown
    if (topics.length === 0) {
        await loadTopics();
    }

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
                            <textarea id="quizDescription" rows="2"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Link to Chapter (Optional)</label>
                            <select id="quizChapter" onchange="updateTopicDropdown()">
                                <option value="">None (General Quiz)</option>
                                ${chapters.map(ch => `<option value="${ch.id}">${ch.title}</option>`).join('')}
                            </select>
                            <small class="help-text">Associate this quiz with a specific chapter</small>
                        </div>
                        <div class="form-group">
                            <label>Link to Topic (Optional)</label>
                            <select id="quizTopic" disabled>
                                <option value="">None (Chapter Level)</option>
                            </select>
                            <small class="help-text">Select a chapter first to see topics</small>
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

function updateTopicDropdown() {
    const chapterId = document.getElementById('quizChapter').value;
    const topicSelect = document.getElementById('quizTopic');

    if (!chapterId) {
        topicSelect.disabled = true;
        topicSelect.innerHTML = '<option value="">None (Chapter Level)</option>';
        return;
    }

    // Filter topics by selected chapter
    const chapterTopics = topics.filter(t => t.chapter_id == chapterId);

    topicSelect.disabled = false;
    topicSelect.innerHTML = `
        <option value="">None (Chapter Level)</option>
        ${chapterTopics.map(t => `<option value="${t.id}">${t.title}</option>`).join('')}
    `;
}

async function saveQuiz() {
    const title = document.getElementById('quizTitle').value;
    const description = document.getElementById('quizDescription').value;
    const chapter_id = document.getElementById('quizChapter').value || null;
    const topic_id = document.getElementById('quizTopic').value || null;
    const order_index = document.getElementById('quizOrder').value;

    if (!title) {
        alert('Please enter a quiz title');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/quizzes.php`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, description, chapter_id, topic_id, order_index })
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

// ==== CLOSE MODAL (CLEANUP QUILL) ====

function closeModal() {
    document.getElementById('modalContainer').innerHTML = '';
    quillEditor = null; // Clear the Quill editor instance
}
