"use client"

import * as React from "react"
import {
  Briefcase,
  FileUser,
  FolderClosed,
  GraduationCap,
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
      title: "Resume",
      url: "",
      icon: FileUser,
      isActive: true,
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
    }
  ],
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
    <Sidebar collapsible="icon" className="top-14" {...props}>
      <SidebarContent>
        <NavMain items={data.navMain} />
      </SidebarContent>
      <SidebarFooter>
        <StatusDialog />
      </SidebarFooter>
    </Sidebar>
  )
}
