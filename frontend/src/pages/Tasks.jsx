import { useState, useEffect } from 'react';
import TaskCard from '../components/TaskCard';
import TaskForm from '../components/TaskForm';
import { taskAPI } from '../services/api';

const Tasks = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [filter, setFilter] = useState('all');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const response = await taskAPI.getAll();
      const data = response.data.tasks || response.data || [];
      setTasks(data);
    } catch (err) {
      setError('Failed to load tasks. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (formData) => {
    try {
      await taskAPI.create(formData);
      setShowForm(false);
      fetchTasks();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create task.');
    }
  };

  const handleUpdate = async (formData) => {
    try {
      await taskAPI.update(editingTask._id, formData);
      setEditingTask(null);
      fetchTasks();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update task.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    try {
      await taskAPI.delete(id);
      fetchTasks();
    } catch (err) {
      setError('Failed to delete task.');
    }
  };

  const handleEdit = (task) => {
    setEditingTask(task);
    setShowForm(false);
  };

  const cancelForm = () => {
    setShowForm(false);
    setEditingTask(null);
  };

  const filteredTasks = tasks.filter((task) => {
    const matchesFilter = filter === 'all' || task.status === filter;
    const matchesSearch = task.title.toLowerCase().includes(search.toLowerCase()) ||
      (task.description && task.description.toLowerCase().includes(search.toLowerCase()));
    return matchesFilter && matchesSearch;
  });

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading tasks...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="tasks-header">
        <div>
          <h1 className="page-title">Tasks</h1>
          <p className="page-subtitle">Manage and organize all your tasks.</p>
        </div>
        {!showForm && !editingTask && (
          <button onClick={() => setShowForm(true)} className="btn btn-primary">
            + New Task
          </button>
        )}
      </div>

      {error && <div className="error-banner">{error}</div>}

      {(showForm || editingTask) && (
        <div className="form-container">
          <TaskForm
            initialData={editingTask}
            onSubmit={editingTask ? handleUpdate : handleCreate}
            onCancel={cancelForm}
          />
        </div>
      )}

      <div className="tasks-toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="Search tasks..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          className="filter-select"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        >
          <option value="all">All Status</option>
          <option value="pending">Pending</option>
          <option value="in-progress">In Progress</option>
          <option value="completed">Completed</option>
        </select>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="empty-state">
          <p className="empty-text">
            {tasks.length === 0
              ? 'No tasks yet. Click "New Task" to create one.'
              : 'No tasks match your filters.'}
          </p>
        </div>
      ) : (
        <div className="tasks-grid">
          {filteredTasks.map((task) => (
            <TaskCard
              key={task._id}
              task={task}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Tasks;
