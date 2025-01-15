import { LoadingProps } from '../../types';

const LoadingScreen = ({ size = 'medium', message = 'Loading...' }: LoadingProps) => {
  const sizeClasses = {
    small: 'w-6 h-6',
    medium: 'w-8 h-8',
    large: 'w-12 h-12'
  };

  return (
    <div className="fixed inset-0 flex flex-col items-center justify-center bg-white bg-opacity-90 z-50">
      <div className={`loader ${sizeClasses[size]}`} />
      {message && (
        <p className="mt-4 text-gray-600 text-lg font-medium animate-pulse">
          {message}
        </p>
      )}
    </div>
  );
};

export default LoadingScreen;
