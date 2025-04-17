"use client";

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useToast } from './ToastProvider';

interface ResumeProcessingContextType {
    activeResumeId: number | null;
    setActiveResumeId: (id: number | null) => void;
    activeFreeformId: number | null;
    setActiveFreeformId: (id: number | null) => void;
}

const ResumeProcessingContext = createContext<ResumeProcessingContextType | undefined>(undefined);

export function ResumeProcessingProvider({ children }: { children: React.ReactNode }) {
    const [activeResumeId, setActiveResumeId] = useState<number | null>(null);
    const [activeFreeformId, setActiveFreeformId] = useState<number | null>(null);
    const { showSuccess, showError } = useToast();

    // Handle resume processing notifications
    useEffect(() => {
        if (!activeResumeId) return;

        console.log('Connecting to SSE for resumeId:', activeResumeId);
        const eventSource = new EventSource(`http://localhost:8080/api/resumes/${activeResumeId}/status`);
        let retryCount = 0;
        const maxRetries = 3;

        eventSource.onopen = () => {
            console.log('SSE connection opened for resume');
            retryCount = 0; // Reset retry count on successful connection
        };

        eventSource.addEventListener('processing-complete', (event) => {
            console.log('Received processing-complete event:', event.data);
            showSuccess('Resume processed!');
            setActiveResumeId(null);
            eventSource.close();
        });

        eventSource.onerror = (error) => {
            console.error('Resume SSE Error:', error);
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('Resume SSE connection closed');
                if (retryCount < maxRetries) {
                    retryCount++;
                    console.log(`Retrying connection (${retryCount}/${maxRetries})...`);
                    // Wait for a second before retrying
                    setTimeout(() => {
                        eventSource.close();
                        new EventSource(`http://localhost:8080/api/resumes/${activeResumeId}/status`);
                    }, 1000);
                } else {
                    showError('Connection to server lost. Please try again.');
                    setActiveResumeId(null);
                }
            }
        };

        return () => {
            console.log('Cleaning up resume SSE connection');
            eventSource.close();
        };
    }, [activeResumeId, showSuccess, showError]);

    // Handle career processing notifications
    useEffect(() => {
        if (!activeFreeformId) return;

        console.log('Connecting to SSE for freeform entry:', activeFreeformId);
        const eventSource = new EventSource(`http://localhost:8080/api/resumes/career/${activeFreeformId}/status`);
        let retryCount = 0;
        const maxRetries = 3;
        let isCompleted = false;

        const cleanup = () => {
            if (!isCompleted) {
                console.log('Cleaning up career SSE connection');
                eventSource.close();
                setActiveFreeformId(null);
            }
        };

        eventSource.onopen = () => {
            console.log('Career SSE connection opened');
            retryCount = 0; // Reset retry count on successful connection
        };

        eventSource.addEventListener('processing-complete', (event) => {
            console.log('Received career processing-complete event:', event.data);
            isCompleted = true;
            showSuccess(event.data || 'Career information processed successfully!');
            setActiveFreeformId(null);
            eventSource.close();
        });

        eventSource.addEventListener('processing-error', (event) => {
            console.log('Received career processing-error event:', event.data);
            isCompleted = true;
            showError(event.data || 'Failed to process career entry');
            setActiveFreeformId(null);
            eventSource.close();
        });

        eventSource.onerror = (error) => {
            console.error('Career SSE Error:', error);
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('Career SSE connection closed');
                if (retryCount < maxRetries) {
                    retryCount++;
                    console.log(`Retrying connection (${retryCount}/${maxRetries})...`);
                    // Wait for a second before retrying
                    setTimeout(() => {
                        eventSource.close();
                        new EventSource(`http://localhost:8080/api/resumes/career/${activeFreeformId}/status`);
                    }, 1000);
                } else {
                    showError('Connection to server lost. Please try again.');
                    cleanup();
                }
            }
        };

        // Set a timeout for the entire operation
        const timeout = setTimeout(() => {
            if (!isCompleted) {
                console.log('Operation timed out');
                showError('Operation timed out. Please try again.');
                cleanup();
            }
        }, 30000); // 30 second timeout

        return () => {
            clearTimeout(timeout);
            cleanup();
        };
    }, [activeFreeformId, showSuccess, showError]);

    return (
        <ResumeProcessingContext.Provider value={{ 
            activeResumeId, 
            setActiveResumeId,
            activeFreeformId,
            setActiveFreeformId
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