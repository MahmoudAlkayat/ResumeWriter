"use client"

import * as React from "react"
import {
  Briefcase,
  FileUser,
  FolderClosed,
  GraduationCap,
  Sparkles,
} from "lucide-react"

import { NavMain } from "@/components/nav-main"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
} from "@/components/ui/sidebar"
import { StatusDialog } from "@/components/StatusDialog"

const data = {
  navMain: [
    {
      title: "Your Resumes",
      url: "",
      icon: FileUser,
      items: [
        {
          title: "Upload",
          url: "/resumes/upload",
        },
        {
          title: "History",
          url:"/resumes/upload/history"
        }
      ],
    },
    {
      title: "Generate Resumes",
      url: "/resumes/generate",
      icon: Sparkles,
      items: [
        {
          title: "Generate",
          url: "/resumes/generate"
        },
        {
          title: "History",
          url: "/resumes/generate/history"
        }
      ]
    },
    {
      title: "Jobs",
      url: "",
      icon: Briefcase,
      items: [
        {
          title: "Submit",
          url: "/jobs/submit",
        },
        {
          title: "History",
          url: "/jobs/history",
        }
      ]
    },
    {
      title: "Career",
      url: "/career",
      icon: FolderClosed,
      items: [
        {
          title: "View",
          url: "/career"
        },
        {
          title: "Freeform",
          url: "/career/freeform/"
        }
      ]
    },
    {
      title: "Education",
      url: "/education",
      icon: GraduationCap,
    }
  ],
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
    <Sidebar collapsible="icon" className="top-14" {...props}>
      <SidebarContent>
        <NavMain items={data.navMain} />
      </SidebarContent>
    </Sidebar>
  )
}
