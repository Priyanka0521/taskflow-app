import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { taskAPI } from '../services/api';

const Dashboard = () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const [tasks, setTasks] = useState([]);
  const [stats, setStats] = useState({ total: 0, pending: 0, inProgress: 0, completed: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const response = await taskAPI.getAll();
      const data = response.data.tasks || response.data || [];
      setTasks(data);
      setStats({
        total: data.length,
        pending: data.filter((t) => t.status === 'pending').length,
        inProgress: data.filter((t) => t.status === 'in-progress').length,
        completed: data.filter((t) => t.status === 'completed').length,
      });
    } catch (err) {
      console.error('Failed to fetch tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="dashboard-header">
        <div>
          <h1 className="page-title">Welcome, {user?.name || 'User'}!</h1>
          <p className="page-subtitle">Here&apos;s what&apos;s happening with your tasks today.</p>
        </div>
        <Link to="/tasks" className="btn btn-primary">
          View All Tasks
        </Link>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-total">
          <div className="stat-label">Total Tasks</div>
          <div className="stat-value">{stats.total}</div>
        </div>
        <div className="stat-card stat-pending">
          <div className="stat-label">Pending</div>
          <div className="stat-value">{stats.pending}</div>
        </div>
        <div className="stat-card stat-progress">
          <div className="stat-label">In Progress</div>
          <div className="stat-value">{stats.inProgress}</div>
        </div>
        <div className="stat-card stat-completed">
          <div className="stat-label">Completed</div>
          <div className="stat-value">{stats.completed}</div>
        </div>
      </div>

      <div className="dashboard-section">
        <div className="section-header">
          <h2 className="section-title">Recent Tasks</h2>
          <Link to="/tasks" className="section-link">
            See all &rarr;
          </Link>
        </div>

        {tasks.length === 0 ? (
          <div className="empty-state">
            <p className="empty-text">No tasks yet. Create your first task to get started!</p>
            <Link to="/tasks" className="btn btn-primary">
              Create Task
            </Link>
          </div>
        ) : (
          <div className="task-list-mini">
            {tasks.slice(0, 5).map((task) => (
              <div key={task._id} className="task-item-mini">
                <div className="task-item-info">
                  <span className="task-item-title">{task.title}</span>
                  <span className={`task-item-status status-${task.status || 'pending'}`}>
                    {task.status || 'pending'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
