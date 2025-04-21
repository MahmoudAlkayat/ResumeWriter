"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";
import { useResumeProcessing } from "@/contexts/ResumeProcessingProvider";
import { Pencil } from "lucide-react";
import LoadingScreen from "@/components/LoadingScreen";
import { Textarea } from "@/components/ui/textarea";

interface FreeformEntry {
    id: number;
    text: string;
    updatedAt: string;
    careerId: number;
}

export default function FreeformCareerPage() {
    const { user } = useAuth();
    const { showError, showSuccess } = useToast();
    const [isLoading, setIsLoading] = useState(true);
    const { addActiveProcess } = useResumeProcessing();

    const [freeformEntries, setFreeformEntries] = useState<FreeformEntry[]>([]);

    // Form state for adding or editing
    const [editingIndex, setEditingIndex] = useState<number | null>(null);
    const [formData, setFormData] = useState<{ text: string }>({
        text: "",
    });

    const fetchFreeformEntries = async () => {
        if (!user?.id) return;
        try {
            const response = await fetch('http://localhost:8080/api/resumes/career/freeform', {
                credentials: "include"
            });
            if (!response.ok) {
                throw new Error("Failed to fetch freeform entries");
            }
            const data = await response.json();
            console.log(data)
            setFreeformEntries(data);
            setIsLoading(false);
        } catch (err) {
            if (err instanceof Error) showError(err.message);
            else showError("An unknown error occurred");
        }
    };

    useEffect(() => {
        fetchFreeformEntries();
    }, [user?.id]);

    // Handle starting the "edit" flow
    const handleEdit = (index: number) => {
        setEditingIndex(index);
        setFormData({ text: freeformEntries[index].text });
    };

    // Handle form field changes
    const handleTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const { value } = e.target;
        setFormData({ text: value });
    };

    // Handle save: CREATE (if editingIndex = -1) or UPDATE
    const handleSave = async () => {
        if (!user?.id) {
            showError("Not authenticated");
            return;
        }

        // Validation
        if (!formData.text.trim()) {
            showError("Please enter some text");
            return;
        }

        // No changes
        if (editingIndex !== null && formData.text.trim() === freeformEntries[editingIndex].text.trim()) {
            setEditingIndex(null);
            setFormData({ text: "" });
            return;
        }

        try {
            if (editingIndex == null || editingIndex < 0) {
                showError("Invalid index for update");
                return;
            }

            const entryId = freeformEntries[editingIndex].id;
            const response = await fetch(
                `http://localhost:8080/api/resumes/career/freeform/${entryId}`,
                {
                    method: "PUT",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ text: formData.text }),
                }
            );

            if (!response.ok) {
                const errText = await response.text();
                throw new Error(errText || "Failed to save freeform entry");
            }

            const result = await response.json();
            addActiveProcess(result.statusId, 'freeform');
            showSuccess("Freeform entry updated. Please wait while we process it.");
            await fetchFreeformEntries();
        } catch (err) {
            if (err instanceof Error) {
                showError(err.message);
            } else {
                showError("An unknown error occurred");
            }
        } finally {
            setEditingIndex(null);
            setFormData({ text: "" });
        }
    };

    if (isLoading) {
        return <LoadingScreen />;
    }

    return (
        <Background className="relative flex flex-col items-center justify-start min-h-screen p-8">
            <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
                Freeform Entries
            </h2>

            {/* List of Freeform Entry Cards */}
            <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8
            dark:bg-neutral-900 dark:border-neutral-800">
                {freeformEntries.length === 0 ? (
                    <p className="text-xl text-foreground drop-shadow-sm text-center">
                        No freeform entries found.
                        <br/>
                        Start by adding a new career entry with freeform text.
                    </p>
                ) : (
                    <div className="space-y-8">
                        {freeformEntries.map((entry, index) => {
                            const isEditing = editingIndex === index;
                            return (
                                <Card
                                    key={entry.id}
                                    className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
                                >
                                        {isEditing ? (
                                            <CardContent className="flex flex-col gap-3">
                                                <Textarea
                                                    className="dark:border-neutral-700 min-h-[200px]"
                                                    placeholder="Enter your career experience..."
                                                    value={formData.text}
                                                    onChange={handleTextareaChange}
                                                />
                                                <div className="flex gap-3 mt-4">
                                                    <Button
                                                        onClick={handleSave}
                                                        className="bg-blue-600 text-white hover:bg-blue-700"
                                                    >
                                                        Save
                                                    </Button>
                                                    <Button
                                                        variant="secondary"
                                                        onClick={() => {
                                                            setEditingIndex(null);
                                                            setFormData({ text: "" });
                                                        }}
                                                    >
                                                        Cancel
                                                    </Button>
                                                </div>
                                        </CardContent>
                                        ) : (
                                            <>
                                            <CardContent>
                                                <div className="flex flex-col">
                                                    <div className="relative flex items-center mb-4">
                                                        <div className="absolute right-0">
                                                            <Button
                                                                onClick={() => handleEdit(index)}
                                                                className="bg-blue-600 text-white hover:bg-blue-700"
                                                            >
                                                                <Pencil size={16} />
                                                            </Button>
                                                        </div>
                                                    </div>
                                                    <p className="text-gray-700 dark:text-white leading-relaxed whitespace-pre-wrap">
                                                        {entry.text}
                                                    </p>
                                                </div>
                                                </CardContent>
                                                <CardFooter className="-mt-2">
                                                    <div className="flex justify-between w-full items-center italic">
                                                        <p className="text-muted-foreground text-sm">
                                                        Last Updated: {new Date(entry.updatedAt).toLocaleString('en-US', {
                                                            dateStyle: 'medium',
                                                            timeStyle: 'short'
                                                        })}
                                                        </p>
                                                        {entry.careerId ? (
                                                            <p className="text-muted-foreground text-xs">
                                                                For CareerID: {entry.careerId}
                                                            </p>
                                                        ) : (
                                                            <p className="text-red-600 text-xs">Failed to generate a career entry</p>
                                                        )}
                                                    </div>
                                                </CardFooter>
                                                </>
                                        )}
                                </Card>
                            );
                        })}
                    </div>
                )}
            </div>
        </Background>
    );
}
