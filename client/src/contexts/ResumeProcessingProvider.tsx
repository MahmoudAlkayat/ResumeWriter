"use client";

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useToast } from './ToastProvider';

interface ActiveProcess {
  id: number;
  type: 'generate' | 'upload' | 'freeform';
}

interface ResumeProcessingContextType {
  activeProcesses: ActiveProcess[];
  addActiveProcess: (id: number, type: 'generate' | 'upload' | 'freeform') => void;
  removeActiveProcess: (id: number) => void;
}

const ResumeProcessingContext = createContext<ResumeProcessingContextType | undefined>(undefined);

export function ResumeProcessingProvider({ children }: { children: React.ReactNode }) {
    const [activeProcesses, setActiveProcesses] = useState<ActiveProcess[]>([]);
    const { showSuccess, showError } = useToast();

    const addActiveProcess = useCallback((id: number, type: 'generate' | 'upload' | 'freeform') => {
        setActiveProcesses(prev => [...prev, { id, type }]);
    }, []);

    const removeActiveProcess = useCallback((id: number) => {
        setActiveProcesses(prev => prev.filter(process => process.id !== id));
    }, []);

    // Poll for status updates
    useEffect(() => {
        if (activeProcesses.length === 0) return;

        const checkStatuses = async () => {
            for (const process of activeProcesses) {
                try {
                    console.log("Fetching", process.id)
                    const response = await fetch(`http://localhost:8080/api/resumes/status/${process.id}`, {
                        credentials: 'include'
                    });

                    if (!response.ok) {
                        throw new Error('Failed to fetch status');
                    }

                    const status = await response.json();
                    
                    if (status.status === 'COMPLETED') {
                        let message = '';
                        
                        switch (process.type) {
                            case 'generate':
                            message = 'Resume generation completed successfully!';
                            break;
                            case 'upload':
                            message = 'Resume upload processed successfully!';
                            break;
                            case 'freeform':
                            message = 'Freeform entry processed successfully!';
                            break;
                            default:
                            message = 'Process completed successfully!';
                        }
                        showSuccess(message);
                        removeActiveProcess(process.id);
                    } else if (status.status === 'FAILED') {
                        showError(status.error || 'Processing failed');
                        removeActiveProcess(process.id);
                    }
                } catch (error) {
                    console.error('Error checking status:', error);
                    // Don't remove the process on network errors, let it retry
                }
            }
        };

        // Check immediately and then every 2 seconds
        checkStatuses();
        const interval = setInterval(checkStatuses, 2000);

        return () => clearInterval(interval);
    }, [activeProcesses, showSuccess, showError, removeActiveProcess]);

    return (
        <ResumeProcessingContext.Provider value={{ 
            activeProcesses,
            addActiveProcess,
            removeActiveProcess
        }}>
            {children}
        </ResumeProcessingContext.Provider>
    );
}

export function useResumeProcessing() {
    const context = useContext(ResumeProcessingContext);
    if (context === undefined) {
        throw new Error('useResumeProcessing must be used within a ResumeProcessingProvider');
    }
    return context;
} 