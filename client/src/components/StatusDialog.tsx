import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogTrigger,
  DialogDescription,
} from "@/components/ui/dialog";
import { CircleEllipsis } from "lucide-react";
import { DialogHeader } from "./ui/dialog";
import { SidebarMenu, SidebarMenuItem, SidebarMenuButton } from "./ui/sidebar";
import { useState, useEffect } from "react";
import { useSidebar } from "./ui/sidebar";

interface Status {
  resumeId: string;
  title: string;
  status: "processing" | "completed" | "failed";
  error?: string;
}

const mockStatuses: Status[] = [
  {
    resumeId: "1",
    title: "Resume 1",
    status: "completed",
  },
  {
    resumeId: "2",
    title: "Resume 2",
    status: "processing",
  },
  {
    resumeId: "3",
    title: "Resume 3",
    status: "failed",
    error: "Error: Failed to generate resume",
  },
];

export function StatusDialog() {
  const [statuses, setStatuses] = useState<Status[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const { setOpen } = useSidebar();

  useEffect(() => {
    setStatuses(mockStatuses);
  }, []);

  useEffect(() => {
    if (isOpen) {
      setOpen(false);
    }
  }, [isOpen, setOpen]);

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <Dialog modal={false} open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild>
            <SidebarMenuButton className="flex items-center gap-1">
                {/* TODO: Fix collapsed icon display */}
              <CircleEllipsis className="scale-200" /> 
              <span className="text-xs font-medium">Status</span>
            </SidebarMenuButton>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Status</DialogTitle>
            </DialogHeader>
            <DialogDescription>
                <div className="flex flex-col gap-2">
              {statuses.length > 0 ? (
                statuses.map((status) => (
                  <div key={status.resumeId} className="flex gap-2">
                    <h3>{status.title}</h3>
                    <p>{status.status}</p>
                    {status.error && <p>{status.error}</p>}
                  </div>
                ))
              ) : (
                <>No generated resumes found</>
              )}
              </div>
            </DialogDescription>
          </DialogContent>
        </Dialog>
      </SidebarMenuItem>
    </SidebarMenu>
  );
}
