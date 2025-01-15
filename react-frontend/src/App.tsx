import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { checkAuthState } from './store/slices/authSlice';
import { RootState } from './store';

// Layout components
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoadingScreen from './components/common/LoadingScreen';

// Pages
import Home from './pages/Home';
import Login from './pages/Login';
import ResumeAnalysis from './pages/ResumeAnalysis';
import Jobs from './pages/Jobs';
import ProfileAssistant from './pages/ProfileAssistant';
import NotFound from './pages/NotFound';

function App() {
  const dispatch = useDispatch();
  const { loading } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    dispatch(checkAuthState());
  }, [dispatch]);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />

        {/* Protected routes */}
        <Route element={<Layout />}>
          <Route path="/" element={
            <ProtectedRoute>
              <Home />
            </ProtectedRoute>
          } />
          
          <Route path="/resume-analysis" element={
            <ProtectedRoute>
              <ResumeAnalysis />
            </ProtectedRoute>
          } />
          
          <Route path="/jobs" element={
            <ProtectedRoute>
              <Jobs />
            </ProtectedRoute>
          } />
          
          <Route path="/profile-assistant" element={
            <ProtectedRoute>
              <ProfileAssistant />
            </ProtectedRoute>
          } />
        </Route>

        {/* 404 and catch-all */}
        <Route path="/404" element={<NotFound />} />
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
