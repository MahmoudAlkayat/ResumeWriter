"use client";

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useToast } from './ToastProvider';

interface ResumeProcessingContextType {
    activeResumeId: number | null;
    setActiveResumeId: (id: number | null) => void;
    activeCareerUserId: number | null;
    setActiveCareerUserId: (id: number | null) => void;
}

const ResumeProcessingContext = createContext<ResumeProcessingContextType | undefined>(undefined);

export function ResumeProcessingProvider({ children }: { children: React.ReactNode }) {
    const [activeResumeId, setActiveResumeId] = useState<number | null>(null);
    const [activeCareerUserId, setActiveCareerUserId] = useState<number | null>(null);
    const { showSuccess, showError } = useToast();

    // Handle resume processing notifications
    useEffect(() => {
        if (!activeResumeId) return;

        console.log('Connecting to SSE for resumeId:', activeResumeId);
        const eventSource = new EventSource(`http://localhost:8080/api/resumes/${activeResumeId}/status`);

        eventSource.onopen = () => {
            console.log('SSE connection opened');
        };

        eventSource.addEventListener('processing-complete', (event) => {
            console.log('Received processing-complete event:', event.data);
            try {
                const data = JSON.parse(event.data);
                showSuccess('Resume processed!');
                setActiveResumeId(null); // Clear the active resume ID after completion
            } catch (error) {
                console.error('Error parsing SSE data:', error);
                showError('Error processing resume data');
            }
            eventSource.close();
        });

        eventSource.onerror = (error) => {
            console.error('SSE Error:', error);
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('SSE connection closed');
                showError('Connection to server lost. Please refresh the page.');
            } else {
                showError('Failed to process resume. Please try again.');
            }
            eventSource.close();
        };

        return () => {
            console.log('Cleaning up SSE connection');
            eventSource.close();
        };
    }, [activeResumeId, showSuccess, showError]);

    // Handle career processing notifications
    useEffect(() => {
        //TODO: Implement when freeform DB table is created
        return;
        if (!activeCareerUserId) return;

        console.log('Connecting to SSE for career processing userId:', activeCareerUserId);
        const eventSource = new EventSource(`http://localhost:8080/api/resumes/career/${activeCareerUserId}/status`);

        eventSource.onopen = () => {
            console.log('Career SSE connection opened');
        };

        eventSource.addEventListener('processing-complete', (event) => {
            console.log('Received career processing-complete event:', event.data);
            try {
                const data = JSON.parse(event.data);
                showSuccess('Freeform career entry processed!');
                setActiveCareerUserId(null); // Clear the active user ID after completion
            } catch (error) {
                console.error('Error parsing career SSE data:', error);
                showError('Error processing career data');
            }
            eventSource.close();
        });

        eventSource.addEventListener('processing-error', (event) => {
            console.log('Received career processing-error event:', event.data);
            try {
                const data = JSON.parse(event.data);
                showError(data.message || 'Failed to process career entry');
                setActiveCareerUserId(null); // Clear the active user ID after error
            } catch (error) {
                console.error('Error parsing career error SSE data:', error);
                showError('Error processing career data');
            }
            eventSource.close();
        });

        eventSource.onerror = (error) => {
            console.error('Career SSE Error:', error);
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('Career SSE connection closed');
                showError('Connection to server lost. Please refresh the page.');
            } else {
                showError('Failed to process career entries. Please try again.');
            }
            eventSource.close();
        };

        return () => {
            console.log('Cleaning up career SSE connection');
            eventSource.close();
        };
    }, [activeCareerUserId, showSuccess, showError]);

    return (
        <ResumeProcessingContext.Provider value={{ 
            activeResumeId, 
            setActiveResumeId,
            activeCareerUserId,
            setActiveCareerUserId
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