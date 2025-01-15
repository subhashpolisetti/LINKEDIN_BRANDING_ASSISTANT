import { Link, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { signOut } from '../../store/slices/authSlice';
import { RootState } from '../../store';

const Navbar = () => {
  const location = useLocation();
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.auth);

  const isActive = (path: string) => location.pathname === path;

  const handleLogout = () => {
    dispatch(signOut());
  };

  return (
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center py-4">
          <Link to="/" className="text-xl font-bold text-primary">
            LinkedIn Job Assistant
          </Link>
          
          <div className="flex items-center gap-6">
            <nav className="flex items-center gap-6">
              <Link
                to="/resume-analysis"
                className={`nav-link ${isActive('/resume-analysis') ? 'active' : ''}`}
              >
                Resume Analysis
              </Link>
              <Link
                to="/jobs"
                className={`nav-link ${isActive('/jobs') ? 'active' : ''}`}
              >
                Latest Jobs
              </Link>
              <Link
                to="/profile-assistant"
                className={`nav-link ${isActive('/profile-assistant') ? 'active' : ''}`}
              >
                Profile Assistant
              </Link>
            </nav>

            {user && (
              <div className="flex items-center gap-4">
                <div className="profile-button">
                  <i className="fas fa-user-circle"></i>
                  <span className="truncate max-w-[200px]">{user.email}</span>
                </div>
                <button onClick={handleLogout} className="logout-button">
                  <i className="fas fa-sign-out-alt"></i>
                  <span>Logout</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
