import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:5000/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Get token from localStorage or session
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized errors
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Redirect to login page
        window.location.href = '/login';
        return Promise.reject(error);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// Auth endpoints
export const auth = {
  getCurrentUser: () => api.get('/auth/user'),
  login: (code: string) => api.get(`/auth/callback?code=${code}`),
  logout: () => api.post('/auth/logout'),
  getLoginUrl: () => api.get('/auth/login-url'),
};

// Resume endpoints
export const resume = {
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('resume', file);
    return api.post('/resume', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  analyze: (resumeId: string, jobDescription: string) =>
    api.post('/analyze', { resume_id: resumeId, job_description: jobDescription }),
  tailor: (resumeId: string, jobDescription: string, keywords: string[]) =>
    api.post('/tailor', {
      resume_id: resumeId,
      job_description: jobDescription,
      keywords,
    }),
  delete: (id: string) => api.delete(`/resume/${id}`),
  getAll: () => api.get('/resumes'),
};

// Jobs endpoints
export const jobs = {
  getAll: () => api.get('/jobs'),
  getById: (id: string) => api.get(`/jobs/${id}`),
  refresh: () => api.post('/jobs/refresh'),
  search: (keyword: string) => api.get(`/jobs/search?keyword=${keyword}`),
  filter: (filters: Record<string, any>) => api.post('/jobs/filter', filters),
  getStats: () => api.get('/jobs/stats'),
};

// Profile endpoints
export const profile = {
  generate: (file: File) => {
    const formData = new FormData();
    formData.append('resume', file);
    return api.post('/profile/generate', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  generateFromExisting: (resumeId: string) =>
    api.post(`/profile/generate/${resumeId}`),
  update: (profileData: any) => api.put('/profile/update', profileData),
  analyze: (profileData: any) => api.post('/profile/analyze', profileData),
};

export default api;
