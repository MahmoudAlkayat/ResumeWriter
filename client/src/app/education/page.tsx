"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { AlertTitle } from "@/components/ui/alert";
import LoadingScreen from "@/components/LoadingScreen";
import { Card, CardContent } from "@/components/ui/card";

interface EducationEntry {
  id?: number; // for existing records
  degree: string;
  institution: string;
  startDate: string;
  endDate: string;
  gpa: number;
}

interface UserResponse {
  id: number;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
}

export default function EducationManager() {
  const router = useRouter();

  // Logged-in user ID (fetched via /auth/me)
  const [userId, setUserId] = useState<number | null>(null);

  // Education data
  const [education, setEducation] = useState<EducationEntry[]>([]);

  // For controlling loading and error states
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Form state for adding or editing
  // If editingIndex = -1 => we are ADDING a new record
  // If editingIndex is a valid array index => we are EDITING that existing record
  // If editingIndex = null => we are not currently in an add/edit flow
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [formData, setFormData] = useState<EducationEntry>({
    degree: "",
    institution: "",
    startDate: "",
    endDate: "",
    gpa: 0,
  });

  // 1) On mount, check auth and fetch education
  useEffect(() => {
    async function initPage() {
      try {
        // Check if user is logged in
        const userRes = await fetch("http://localhost:8080/auth/me", {
          credentials: "include",
        });
        if (!userRes.ok) {
          // Not authenticated => redirect to /login
          router.push("/login");
          return;
        }
        const userData: UserResponse = await userRes.json();
        setUserId(userData.id);

        // Fetch this user's education from the correct endpoint
        const eduRes = await fetch(
          `http://localhost:8080/api/users/${userData.id}/education`,
          { credentials: "include" }
        );
        if (!eduRes.ok) {
          const errText = await eduRes.text();
          throw new Error(errText || "Failed to fetch education");
        }
        const eduJson = await eduRes.json();
        setEducation(eduJson.education || []);
      } catch (err) {
        if (err instanceof Error) setError(err.message);
        else setError("An unknown error occurred");
      } finally {
        setLoading(false);
      }
    }
    initPage();
  }, [router]);

  // 2) Handle starting the “add” flow
  const handleAdd = () => {
    setEditingIndex(-1); // -1 to indicate creating a new record
    setFormData({
      degree: "",
      institution: "",
      startDate: "",
      endDate: "",
      gpa: 0,
    });
  };

  // 3) Handle starting the “edit” flow
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

  // Helper: Refresh the education list
  const refreshEducation = async () => {
    if (!userId) return;
    try {
      const eduRes = await fetch(
        `http://localhost:8080/api/users/${userId}/education`,
        { credentials: "include" }
      );
      if (!eduRes.ok) throw new Error("Failed to fetch education");
      const eduJson = await eduRes.json();
      setEducation(eduJson.education || []);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("An unknown error occurred");
      }
    }
  };

  // 5) Handle save: CREATE (if editingIndex = -1) or UPDATE
  const handleSave = async () => {
    if (!userId) {
      setError("Not authenticated");
      return;
    }

    try {
      if (editingIndex === -1) {
        // CREATE a new record using the correct endpoint
        const res = await fetch(
          `http://localhost:8080/api/users/${userId}/education`,
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
      } else {
        // We’re editing an existing record. Make sure editingIndex isn't null or -1
        if (editingIndex == null || editingIndex < 0) {
          setError("Invalid index for update");
          return;
        }

        const eduId = education[editingIndex].id;
        if (!eduId) {
          setError("Missing education ID");
          return;
        }
        // UPDATE existing record using the correct endpoint
        const res = await fetch(
          `http://localhost:8080/api/users/${userId}/education/${eduId}`,
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
      }
      // Refresh data from server
      await refreshEducation();
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("An unknown error occurred");
      }
    } finally {
      // Clear editing state
      setEditingIndex(null);
      setFormData({
        degree: "",
        institution: "",
        startDate: "",
        endDate: "",
        gpa: 0,
      });
    }
  };

  // 6) Delete an existing record
  const handleDelete = async (index: number) => {
    if (!userId) {
      setError("Not authenticated");
      return;
    }

    const eduId = education[index].id;
    if (!eduId) {
      setError("Invalid record ID");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/api/users/${userId}/education/${eduId}`,
        {
          method: "DELETE",
          credentials: "include",
        }
      );
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Failed to delete education record");
      }
      // Refresh data
      await refreshEducation();
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("An unknown error occurred");
      }
    }
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-extrabold text-black mb-8 drop-shadow-md">
        Manage Education
      </h2>

      {error && (
        <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 max-w-lg mx-auto rounded-lg shadow-md">
          <AlertTitle className="font-bold">Error</AlertTitle>
          <p>{error}</p>
        </div>
      )}

      {/* Add New Education Button */}
      <div className="mb-6">
        <Button onClick={handleAdd} className="bg-green-500 hover:bg-green-600">
          Add New Education
        </Button>
      </div>

      {/* List of Education Cards */}
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200">
        {education.length === 0 ? (
          <p className="text-xl text-gray-800 drop-shadow-sm text-center">
            No education records found.
          </p>
        ) : (
          <div className="space-y-8">
            {education.map((edu, index) => {
              const isEditing = editingIndex === index;
              return (
                <Card
                  key={edu.id ?? index}
                  className="p-8 shadow-md rounded-xl bg-gray-50 border border-gray-300"
                >
                  <CardContent className="flex flex-col gap-3">
                    {isEditing ? (
                      // EDITING FORM
                      <>
                        <input
                          className="border rounded p-2 mb-2"
                          name="degree"
                          placeholder="Degree"
                          value={formData.degree}
                          onChange={handleFormChange}
                        />
                        <input
                          className="border rounded p-2 mb-2"
                          name="institution"
                          placeholder="Institution"
                          value={formData.institution}
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
                        <input
                          className="border rounded p-2 mb-2"
                          type="number"
                          name="gpa"
                          step="0.01"
                          placeholder="GPA"
                          value={String(formData.gpa)}
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
                              // Cancel editing
                              setEditingIndex(null);
                              setFormData({
                                degree: "",
                                institution: "",
                                startDate: "",
                                endDate: "",
                                gpa: 0,
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
                        <h3 className="text-2xl font-bold text-black">
                          {edu.degree}
                        </h3>
                        <p className="text-lg text-gray-700 font-semibold">
                          {edu.institution}
                        </p>
                        <p className="text-md text-gray-500 italic">
                          {edu.startDate} - {edu.endDate}
                        </p>
                        <p className="text-gray-700">GPA: {edu.gpa}</p>
                        <div className="flex gap-3 mt-4">
                          <Button
                            onClick={() => handleEdit(index)}
                            className="bg-blue-600 text-white hover:bg-blue-700"
                          >
                            Edit
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

      {/* If user clicked "Add New Education" (editingIndex = -1) => inline form */}
      {editingIndex === -1 && (
        <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mt-6">
          <h3 className="text-2xl font-bold mb-4">Add New Education</h3>
          <div className="flex flex-col gap-2">
            <input
              className="border rounded p-2"
              name="degree"
              placeholder="Degree"
              value={formData.degree}
              onChange={handleFormChange}
            />
            <input
              className="border rounded p-2"
              name="institution"
              placeholder="Institution"
              value={formData.institution}
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
            <input
              className="border rounded p-2"
              type="number"
              name="gpa"
              step="0.01"
              placeholder="GPA"
              value={String(formData.gpa)}
              onChange={handleFormChange}
            />
          </div>
          <div className="flex gap-3 mt-4">
            <Button
              onClick={handleSave}
              className="bg-green-600 text-white hover:bg-green-700"
            >
              Save New Education
            </Button>
            <Button
              variant="secondary"
              onClick={() => {
                setEditingIndex(null);
                setFormData({
                  degree: "",
                  institution: "",
                  startDate: "",
                  endDate: "",
                  gpa: 0,
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
