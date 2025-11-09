import { useEffect, useState } from 'react';

interface LoadingModalProps {
  isLoading: boolean;
}

export function LoadingModal({ isLoading }: LoadingModalProps) {
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    let timer: NodeJS.Timeout;
    
    if (isLoading) {
      // Show modal after 400ms of loading
      timer = setTimeout(() => {
        setShowModal(true);
      }, 400);
    } else {
      setShowModal(false);
    }

    return () => {
      if (timer) clearTimeout(timer);
    };
  }, [isLoading]);

  if (!showModal) return null;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center">
      <div className="bg-white rounded-lg p-8 shadow-2xl flex flex-col items-center space-y-4">
        <div className="relative w-16 h-16">
          <div className="absolute inset-0 border-4 border-gray-200 rounded-full"></div>
          <div className="absolute inset-0 border-4 border-blue-600 rounded-full border-t-transparent animate-spin"></div>
        </div>
        <p className="text-lg font-medium text-gray-900">Loading...</p>
        <p className="text-sm text-gray-500">Please wait</p>
      </div>
    </div>
  );
}

