"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Card, CardContent } from "@/components/ui/card";
import { useAuth } from "@/hooks/auth";
import { useToast } from "@/contexts/ToastProvider";
import { useResumeProcessing } from "@/contexts/ResumeProcessingProvider";
import { Trash, Pencil, Plus } from "lucide-react";
import LoadingScreen from "@/components/LoadingScreen";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
interface EducationEntry {
  id?: number; // for existing records
  degree: string;
  fieldOfStudy: string;
  institution: string;
  startDate: string;
  endDate: string;
  gpa: number;
  description: string;
}

export default function EducationManager() {
  const { user } = useAuth();
  const { showError, showSuccess } = useToast();
  const { activeResumeId } = useResumeProcessing();
  const [isLoading, setIsLoading] = useState(true);

  // Education data
  const [education, setEducation] = useState<EducationEntry[]>([]);

  // Form state for adding or editing
  // If editingIndex = -1 => we are ADDING a new record
  // If editingIndex is a valid array index => we are EDITING that existing record
  // If editingIndex = null => we are not currently in an add/edit flow
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [formData, setFormData] = useState<EducationEntry>({
    degree: "",
    institution: "",
    fieldOfStudy: "",
    startDate: "",
    endDate: "",
    gpa: 0,
    description: "",
  });

  async function fetchEducation() {
  if (!user?.id) return;
    try {
      // Fetch this user's education from the correct endpoint
      const eduRes = await fetch(
        `http://localhost:8080/api/resumes/education`,
        { credentials: "include" }
      );
      if (!eduRes.ok) {
        const errText = await eduRes.text();
        throw new Error(errText || "Failed to fetch education");
      }
      const eduJson = await eduRes.json();
      setEducation(eduJson.education || []);
      setIsLoading(false);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  }
  // 1) On mount, check auth and fetch education
  useEffect(() => {
    fetchEducation();
  }, [user?.id]);

  useEffect(() => {
    const onUpdate = async () => {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      fetchEducation();
    }
    onUpdate();
  },[activeResumeId])

  // 2) Handle starting the "add" flow
  const handleAdd = () => {
    if (editingIndex == null) {
      setEditingIndex(-1); // -1 to indicate creating a new record
      setFormData({
        degree: "",
        institution: "",
        fieldOfStudy: "",
        startDate: "",
        endDate: "",
        gpa: 0,
        description: "",
      });
    }
    if (editingIndex == -1) {
      setEditingIndex(null); // null to indicate not in an add/edit flow
      setFormData({
        degree: "",
        institution: "",
        fieldOfStudy: "",
        startDate: "",
        endDate: "",
        gpa: 0,
        description: "",
      });
    }
  };

  // 3) Handle starting the "edit" flow
  const handleEdit = (index: number) => {
    setEditingIndex(index);
    setFormData(education[index]);
  };

  // 4) Handle form field changes
  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "gpa" ? Number(value) : value,
    }));
  };

  const handleTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 5) Handle save: CREATE (if editingIndex = -1) or UPDATE
  const handleSave = async () => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    // Validation
    if (!formData.degree || !formData.institution || !formData.fieldOfStudy || !formData.startDate || !formData.endDate) {
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
        // CREATE a new record using the correct endpoint
        const res = await fetch(
          `http://localhost:8080/api/resumes/education`,
          {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(formData),
          }
        );
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to create new education record");
        }
        showSuccess("Education record created successfully");
      } else {
        // We're editing an existing record. Make sure editingIndex isn't null or -1
        if (editingIndex == null || editingIndex < 0) {
          showError("Invalid index for update");
          return;
        }

        const eduId = education[editingIndex].id;
        if (!eduId) {
          showError("Missing education ID");
          return;
        }
        // UPDATE existing record using the correct endpoint
        const res = await fetch(
          `http://localhost:8080/api/resumes/education/${eduId}`,
          {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(formData),
          }
        );
        if (!res.ok) {
          const errText = await res.text();
          throw new Error(errText || "Failed to update education record");
        }
        showSuccess("Education record saved successfully");
      }
      // Refresh data from server
      await fetchEducation();
    } catch (err) {
      if (err instanceof Error) {
        showError(err.message);
      } else {
        showError("An unknown error occurred");
      }
    } finally {
      // Clear editing state
      setEditingIndex(null);
      setFormData({
        degree: "",
        institution: "",
        fieldOfStudy: "",
        startDate: "",
        endDate: "",
        gpa: 0,
        description: "",
      });
    }
  };

  // 6) Delete an existing record
  const handleDelete = async (index: number) => {
    if (!user?.id) {
      showError("Not authenticated");
      return;
    }

    const eduId = education[index].id;
    if (!eduId) {
      showError("Invalid record ID");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/api/resumes/education/${eduId}`,
        {
          method: "DELETE",
          credentials: "include",
        }
      );
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to delete education record");
      }
      showSuccess("Education record deleted successfully");
      // Refresh data
      await fetchEducation();
    } catch (err) {
      if (err instanceof Error) {
        showError(err.message);
      } else {
        showError("An unknown error occurred");
      }
    }
  };

  if (isLoading) {
    return (
      <LoadingScreen/>
    )
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
        Education
      </h2>

      {/* List of Education Cards */}
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8
      dark:bg-neutral-900 dark:border-neutral-800">
        {education.length === 0 ? (
          <p className="text-xl text-foreground drop-shadow-sm text-center">
            No education records found.
          </p>
        ) : (
          <div className="space-y-8">
            {education.map((edu, index) => {
              const isEditing = editingIndex === index;
              return (
                <Card
                  key={edu.id ?? index}
                  className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
                >
                  <CardContent className="flex flex-col gap-3">
                    {isEditing ? (
                      // EDITING FORM
                      <>
                        <Input
                          className="dark:border-neutral-700"
                          name="degree"
                          placeholder="Degree"
                          value={formData.degree}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="institution"
                          placeholder="Institution"
                          value={formData.institution}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="fieldOfStudy"
                          placeholder="Field of Study"
                          value={formData.fieldOfStudy}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="startDate"
                          placeholder="Start Date (YYYY-MM-DD)"
                          value={formData.startDate}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          name="endDate"
                          placeholder="End Date (YYYY-MM-DD)"
                          value={formData.endDate}
                          onChange={handleFormChange}
                        />
                        <Input
                          className="dark:border-neutral-700"
                          type="number"
                          name="gpa"
                          step="0.01"
                          placeholder="GPA"
                          value={String(formData.gpa)}
                          onChange={handleFormChange}
                        />

                        <Textarea
                          className="dark:border-neutral-700"
                          name="description"
                          placeholder="Description"
                          value={formData.description}
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
                              // Cancel editing
                              setEditingIndex(null);
                              setFormData({
                                degree: "",
                                institution: "",
                                fieldOfStudy: "",
                                startDate: "",
                                endDate: "",
                                gpa: 0,
                                description: "",
                              });
                            }}
                          >
                            Cancel
                          </Button>
                        </div>
                      </>
                    ) : (
                      // DISPLAY (NOT EDITING)
                      <>
                        <div className="flex flex-col">
                            {/* Title and buttons inline */}
                            <div className="relative flex items-center">
                                <div className="absolute right-0">
                                    <div className="flex gap-3">
                                        <Button
                                            onClick={() => handleEdit(index)}
                                            className="bg-blue-600 text-white hover:bg-blue-700"
                                        >
                                            <Pencil size={16} />
                                        </Button>
                                        <Button
                                            onClick={() => handleDelete(index)}
                                            className="bg-red-600 text-white hover:bg-red-700"
                                        >
                                            <Trash size={16} />
                                        </Button>
                                    </div>
                                </div>
                                <div className="w-full text-center">
                                    <h3 className="text-2xl font-bold text-primary">
                                        {edu.degree} - {edu.fieldOfStudy}
                                    </h3>
                                </div>
                            </div>
                            
                            {/* Rest of the content */}
                            <p className="text-lg text-gray-700 dark:text-white font-semibold">
                                {edu.institution}
                            </p>
                            <div className="flex items-center justify-center gap-2 text-md text-gray-500 dark:text-muted-foreground italic mb-4">
                                <span>{edu.startDate ? new Date(edu.startDate).toISOString().slice(0, 10) : ""} -{" "}
                                    {edu.endDate === "Present"
                                        ? edu.endDate
                                        : new Date(edu.endDate).toISOString().slice(0, 10)}</span>
                                <span className="text-gray-400">•</span>
                                <span>GPA: {edu.gpa}</span>
                            </div>
                            <p className="text-gray-700 dark:text-white leading-relaxed">
                                {edu.description}
                            </p>
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

      {/* Add New Education Button */}
      {editingIndex == null && (
        <div className="mb-8">
          <Button onClick={handleAdd} className="bg-green-500 hover:bg-green-600 text-white">
            <Plus style={{ scale: 1.35 }} />
        </Button>
      </div>
      )}

      {/* If user clicked "Add New Education" (editingIndex = -1) => inline form */}
      {editingIndex === -1 && (
        <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-800">
          <h3 className="text-2xl font-bold mb-4">New Education Entry</h3>
          <div className="flex flex-col gap-2">
            <Input
              name="degree"
              placeholder="Degree"
              value={formData.degree}
              onChange={handleFormChange}
            />
            <Input
              name="institution"
              placeholder="Institution"
              value={formData.institution}
              onChange={handleFormChange}
            />
            <Input
              name="fieldOfStudy"
              placeholder="Field of Study"
              value={formData.fieldOfStudy}
              onChange={handleFormChange}
            />
            <Input
              name="startDate"
              placeholder="Start Date (YYYY-MM-DD)"
              value={formData.startDate}
              onChange={handleFormChange}
            />
            <Input
              name="endDate"
              placeholder="End Date (YYYY-MM-DD)"
              value={formData.endDate}
              onChange={handleFormChange}
            />
            <Input
              type="number"
              name="gpa"
              step="0.01"
              placeholder="GPA"
              value={String(formData.gpa)}
              onChange={handleFormChange}
            />
            <Textarea
              name="description"
              placeholder="Description"
              value={formData.description}
              onChange={handleTextareaChange}
            />
          </div>
          <div className="flex gap-3 mt-4">
            <Button
              onClick={handleSave}
              className="bg-green-500 text-white hover:bg-green-600"
            >
              Save New Education
            </Button>
            <Button
              className="bg-red-500 text-white hover:bg-red-600"
              onClick={() => {
                setEditingIndex(null);
                setFormData({
                  degree: "",
                  institution: "",
                  fieldOfStudy: "",
                  startDate: "",
                  endDate: "",
                  gpa: 0,
                  description: "",
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
