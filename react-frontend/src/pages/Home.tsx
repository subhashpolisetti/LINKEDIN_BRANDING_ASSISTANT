import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { jobs as jobsApi } from '../services/api';
import LoadingScreen from '../components/common/LoadingScreen';
import { Job } from '../types';

const Home = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    total_jobs: 0,
    recent_jobs: 0,
    last_update: null as string | null
  });
  const [recentJobs, setRecentJobs] = useState<Job[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [statsResponse, jobsResponse] = await Promise.all([
          jobsApi.getStats(),
          jobsApi.getAll()
        ]);

        setStats(statsResponse.data);
        // Get most recent 3 jobs
        setRecentJobs(jobsResponse.data.slice(0, 3));
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-4">
          Welcome back, {user?.name || user?.email}!
        </h1>
        <p className="text-gray-600 text-lg">
          Let's optimize your job search and career profile.
        </p>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-lg p-6">
          <div className="text-4xl font-bold text-primary mb-2">{stats.total_jobs}</div>
          <div className="text-gray-600">Total Available Jobs</div>
        </div>
        <div className="bg-white rounded-lg shadow-lg p-6">
          <div className="text-4xl font-bold text-primary mb-2">{stats.recent_jobs}</div>
          <div className="text-gray-600">New Jobs Today</div>
        </div>
        <div className="bg-white rounded-lg shadow-lg p-6">
          <div className="text-sm text-gray-500 mb-2">Last Updated</div>
          <div className="text-gray-600">
            {stats.last_update ? new Date(stats.last_update).toLocaleString() : 'N/A'}
          </div>
        </div>
      </div>

      {/* Recent Jobs */}
      <div className="bg-white rounded-lg shadow-lg p-8">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Recent Jobs</h2>
          <Link to="/jobs" className="btn-outline-primary">
            <i className="fas fa-briefcase mr-2"></i>
            View All Jobs
          </Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {recentJobs.map((job) => (
            <div key={job.id} className="job-card">
              <h3 className="job-title">{job.title}</h3>
              <p className="company-name">
                <i className="fas fa-building mr-2"></i>
                {job.companyName}
              </p>
              <div className="flex justify-between items-center text-sm text-gray-500 mb-4">
                <span>
                  <i className="fas fa-map-marker-alt mr-1"></i>
                  {job.location}
                </span>
                <span>
                  <i className="fas fa-clock mr-1"></i>
                  {new Date(job.listedAt).toLocaleDateString()}
                </span>
              </div>
              <a href={job.jobPostingUrl} target="_blank" rel="noopener noreferrer" 
                 className="view-job-btn">
                <i className="fas fa-external-link-alt mr-2"></i>
                View Job
              </a>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Link to="/resume-analysis" 
              className="bg-white rounded-lg shadow-lg p-8 hover:shadow-xl transition-shadow">
          <i className="fas fa-file-alt text-4xl text-primary mb-4"></i>
          <h3 className="text-xl font-bold text-gray-800 mb-2">Resume Analysis</h3>
          <p className="text-gray-600">
            Upload your resume and get instant feedback on how well it matches job requirements.
          </p>
        </Link>
        <Link to="/profile-assistant"
              className="bg-white rounded-lg shadow-lg p-8 hover:shadow-xl transition-shadow">
          <i className="fas fa-user-tie text-4xl text-primary mb-4"></i>
          <h3 className="text-xl font-bold text-gray-800 mb-2">Profile Assistant</h3>
          <p className="text-gray-600">
            Generate an optimized LinkedIn profile from your resume using AI.
          </p>
        </Link>
      </div>
    </div>
  );
};

export default Home;
