const TaskCard = ({ task, onEdit, onDelete }) => {
  const priorityColors = {
    low: 'priority-low',
    medium: 'priority-medium',
    high: 'priority-high',
  };

  const statusColors = {
    pending: 'status-pending',
    'in-progress': 'status-in-progress',
    completed: 'status-completed',
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="task-card">
      <div className="task-card-header">
        <h3 className="task-title">{task.title}</h3>
        <span className={`task-priority ${priorityColors[task.priority || 'medium']}`}>
          {task.priority || 'medium'}
        </span>
      </div>

      {task.description && (
        <p className="task-description">{task.description}</p>
      )}

      <div className="task-card-footer">
        <span className={`task-status ${statusColors[task.status || 'pending']}`}>
          {task.status || 'pending'}
        </span>
        {task.dueDate && (
          <span className="task-due">Due: {formatDate(task.dueDate)}</span>
        )}
      </div>

      <div className="task-actions">
        <button onClick={() => onEdit(task)} className="task-btn task-btn-edit">
          Edit
        </button>
        <button onClick={() => onDelete(task._id)} className="task-btn task-btn-delete">
          Delete
        </button>
      </div>
    </div>
  );
};

export default TaskCard;
