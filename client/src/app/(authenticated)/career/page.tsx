"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Background } from "@/components/ui/background";
import { Pencil } from "lucide-react";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";

interface Job {
  id?: number; // For existing records
  title: string;
  company: string;
  startDate: string;
  endDate: string;
  responsibilities: string;
}

export default function CareerHistoryManager() {
  const { user } = useAuth();
  const { showError, showSuccess } = useToast();

  // Career history data
  const [careerHistory, setCareerHistory] = useState<Job[]>([]);

  /**
   * editingIndex indicates which item is being edited:
   *  -1 => adding new,
   *  >=0 => editing that index,
   *  null => neither adding nor editing.
   */
  const [editingIndex, setEditingIndex] = useState<number | null>(null);

  // The form data for either add or edit
  const [formData, setFormData] = useState<Job>({
    title: "",
    company: "",
    startDate: "",
    endDate: "",
    responsibilities: "",
  });

  // 2) Fetch career history whenever userId is available
  useEffect(() => {
    async function fetchCareerHistory() {
      if (!user?.id) return;
      try {
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
          credentials: "include",
        });
        if (!res.ok) throw new Error("Failed to fetch career history");
        const data = await res.json();
        setCareerHistory(data.jobs || []);
      } catch (err) {
        if (err instanceof Error) showError(err.message);
        else showError("An unknown error occurred");
      }
    }
    fetchCareerHistory();
  }, [user?.id]);

  // Start "Add New Career" flow
  const handleAdd = () => {
    setEditingIndex(-1);
    setFormData({
      title: "",
      company: "",
      startDate: "",
      endDate: "",
      responsibilities: "",
    });
  };

  // Start "Edit" flow for a specific job
  const handleEdit = (index: number) => {
    setEditingIndex(index);
    setFormData(careerHistory[index]);
  };

  // Handle changes in the form fields (input & textarea)
  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Handle save: CREATE (if adding new) or UPDATE (if editing)
  const handleSave = async () => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    // Validation
    if (!formData.title || !formData.company || !formData.startDate || !formData.endDate) {
      showError("Please fill in all fields");
      return;
    }

    const startDate = new Date(formData.startDate);
    const endDate = new Date(formData.endDate);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      showError("Invalid date format");
      return;
    }
    if (startDate > endDate) {
      showError("Start date must be before end date");
      return;
    }

    try {
      if (editingIndex === -1) {
        // Create new career record
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        });
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to create career record");
        }
        showSuccess("Career record created successfully");
      } else {
        // Ensure editingIndex is not null and is >= 0
        if (editingIndex === null || editingIndex < 0) {
          showError("Invalid index for update");
          return;
        }
        // Use a local var to ensure TypeScript knows this is a number
        const idx = editingIndex;
        const jobId = careerHistory[idx].id;
        if (!jobId) {
          showError("Missing job ID");
          return;
        }
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career/${jobId}`, {
          method: "PUT",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        });
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to update career record");
        }
        showSuccess("Career record saved successfully");
      }
      // Refresh career history after saving
      if (user?.id) {
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
          credentials: "include",
        });
        if (res.ok) {
          const data = await res.json();
          setCareerHistory(data.jobs || []);
        }
      }
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setEditingIndex(null);
      setFormData({
        title: "",
        company: "",
        startDate: "",
        endDate: "",
        responsibilities: "",
      });
    }
  };

  // Handle deletion of a career record
  const handleDelete = async (index: number) => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }
    const jobId = careerHistory[index].id;
    if (!jobId) {
      showError("Invalid job ID");
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/api/users/${user.id}/career/${jobId}`, {
        method: "DELETE",
        credentials: "include",
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to delete career record");
      }
      showSuccess("Career record deleted successfully");
      // Refresh career history after deletion
      if (user?.id) {
        const res = await fetch(`http://localhost:8080/api/users/${user.id}/career`, {
          credentials: "include",
        });
        if (res.ok) {
          const data = await res.json();
          setCareerHistory(data.jobs || []);
        }
      }
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  };

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-extrabold text-black mb-8 drop-shadow-md">
        Career History
      </h2>

      {/* List of Career Cards */}
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8">
        {careerHistory.length === 0 ? (
          <p className="text-xl text-gray-800 drop-shadow-sm text-center">
            No career history available.
          </p>
        ) : (
          <div className="space-y-8">
            {careerHistory.map((job, index) => {
              const isEditing = editingIndex === index;
              return (
                <Card
                  key={job.id ?? index}
                  className="p-8 shadow-md rounded-xl bg-gray-50 border border-gray-300"
                >
                  <CardContent className="flex flex-col gap-3">
                    {isEditing ? (
                      <>
                        <input
                          className="border rounded p-2 mb-2"
                          name="title"
                          placeholder="Job Title"
                          value={formData.title}
                          onChange={handleFormChange}
                        />
                        <input
                          className="border rounded p-2 mb-2"
                          name="company"
                          placeholder="Company"
                          value={formData.company}
                          onChange={handleFormChange}
                        />
                        <input
                          className="border rounded p-2 mb-2"
                          name="startDate"
                          placeholder="Start Date (YYYY-MM-DD)"
                          value={formData.startDate}
                          onChange={handleFormChange}
                        />
                        <input
                          className="border rounded p-2 mb-2"
                          name="endDate"
                          placeholder="End Date (YYYY-MM-DD)"
                          value={formData.endDate}
                          onChange={handleFormChange}
                        />
                        <textarea
                          className="border rounded p-2 mb-2"
                          name="responsibilities"
                          placeholder="Responsibilities"
                          value={formData.responsibilities}
                          onChange={handleFormChange}
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
                              setFormData({
                                title: "",
                                company: "",
                                startDate: "",
                                endDate: "",
                                responsibilities: "",
                              });
                            }}
                          >
                            Cancel
                          </Button>
                        </div>
                      </>
                    ) : (
                      <>
                        <h3 className="text-2xl font-bold text-black">
                          {job.title}
                        </h3>
                        <p className="text-lg text-gray-700 font-semibold">
                          {job.company}
                        </p>
                        <p className="text-md text-gray-500 italic">
                          {new Date(job.startDate).toLocaleDateString()} -{" "}
                          {job.endDate
                            ? new Date(job.endDate).toLocaleDateString()
                            : "Present"}
                        </p>
                        <p className="text-gray-700 leading-relaxed">
                          {job.responsibilities}
                        </p>
                        <div className="flex gap-3 mt-4">
                          <Button
                            onClick={() => handleEdit(index)}
                            className="bg-blue-600 text-white hover:bg-blue-700"
                          >
                            <Pencil className="w-5 h-5 mr-2" /> Edit
                          </Button>
                          <Button
                            onClick={() => handleDelete(index)}
                            className="bg-red-600 text-white hover:bg-red-700"
                          >
                            Delete
                          </Button>
                        </div>
                      </>
                    )}
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      <div className="mb-8">
        <Button onClick={handleAdd} className="bg-green-500 hover:bg-green-600">
          Add New Career Entry
        </Button>
      </div>

      {/* Inline form for adding a new career entry (if editingIndex is -1) */}
      {editingIndex === -1 && (
        <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200">
          <h3 className="text-2xl font-bold mb-4">New Career Entry</h3>
          <div className="flex flex-col gap-2">
            <input
              className="border rounded p-2"
              name="title"
              placeholder="Job Title"
              value={formData.title}
              onChange={handleFormChange}
            />
            <input
              className="border rounded p-2"
              name="company"
              placeholder="Company"
              value={formData.company}
              onChange={handleFormChange}
            />
            <input
              className="border rounded p-2"
              name="startDate"
              placeholder="Start Date (YYYY-MM-DD)"
              value={formData.startDate}
              onChange={handleFormChange}
            />
            <input
              className="border rounded p-2"
              name="endDate"
              placeholder="End Date (YYYY-MM-DD)"
              value={formData.endDate}
              onChange={handleFormChange}
            />
            <textarea
              className="border rounded p-2"
              name="responsibilities"
              placeholder="Responsibilities"
              value={formData.responsibilities}
              onChange={handleFormChange}
            />
          </div>
          <div className="flex gap-3 mt-4">
            <Button onClick={handleSave} className="bg-green-600 text-white hover:bg-green-700">
              Save New Career Entry
            </Button>
            <Button
              variant="secondary"
              onClick={() => {
                setEditingIndex(null);
                setFormData({
                  title: "",
                  company: "",
                  startDate: "",
                  endDate: "",
                  responsibilities: "",
                });
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}
    </Background>
  );
}
