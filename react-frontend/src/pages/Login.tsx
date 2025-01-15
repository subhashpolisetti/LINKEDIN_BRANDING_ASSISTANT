import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { auth } from '../services/api';

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const from = (location.state as any)?.from?.pathname || '/';

  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, from]);

  const handleLogin = async () => {
    try {
      const response = await auth.getLoginUrl();
      window.location.href = response.data.loginUrl;
    } catch (error) {
      console.error('Failed to get login URL:', error);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-lg p-8 text-center">
        <i className="fas fa-user-circle text-6xl text-primary mb-6"></i>
        <h1 className="text-3xl font-bold text-gray-800 mb-4">
          Welcome to LinkedIn Job Assistant
        </h1>
        <p className="text-gray-600 mb-8 text-lg">
          Your intelligent career companion for job search and resume optimization.
        </p>
        <button onClick={handleLogin} className="btn-primary inline-flex items-center gap-2">
          <i className="fas fa-sign-in-alt"></i>
          Login with Cognito
        </button>
      </div>
    </div>
  );
};

export default Login;
