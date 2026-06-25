import React, { useState } from 'react';
import type { Course } from '../../../shared/types';
import { BookOpen, MapPin, User, Award, Calendar, ChevronRight } from 'lucide-react';

interface Props {
  courses: Course[];
  onQuickAction?: (actionText: string) => void;
}

export default function ScheduleCard({ courses, onQuickAction }: Props) {
  const [activeDay, setActiveDay] = useState<number>(new Date().getDay() || 1);
  const adjustedDay = activeDay > 5 ? 1 : activeDay;
  const days = [{ label: '周一', value: 1 },{ label: '周二', value: 2 },{ label: '周三', value: 3 },{ label: '周四', value: 4 },{ label: '周五', value: 5 }];
  const filteredCourses = courses.filter(c => c.weekday === adjustedDay);
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);

  return (
    <div className="bg-white rounded-[24px] border border-neutral-100 p-8 shadow-[0_8px_30px_rgb(0,0,0,0.012)] transition-all hover:shadow-[0_12px_40px_rgb(0,0,0,0.018)]">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <span className="text-xs font-mono text-neutral-400 uppercase tracking-widest">MODULE 01 / SCHEDULE</span>
          <h2 className="text-xl font-semibold text-neutral-900 tracking-tight mt-1 flex items-center gap-2">今日课表 <span className="text-xs px-2 py-0.5 rounded-full bg-neutral-100 text-neutral-600 font-normal">Week 16</span></h2>
        </div>
        <div className="flex items-center gap-1 bg-neutral-50 p-1 rounded-xl border border-neutral-100">
          {days.map(d => (
            <button key={d.value} onClick={() => setActiveDay(d.value)}
              className={`px-3.5 py-1.5 text-xs font-medium rounded-lg transition-all ${adjustedDay === d.value ? 'bg-white text-neutral-950 shadow-[0_2px_8px_rgba(0,0,0,0.04)]' : 'text-neutral-400 hover:text-neutral-700'}`}>{d.label}</button>
          ))}
        </div>
      </div>
      <div className="space-y-4">
        {filteredCourses.length > 0 ? filteredCourses.map(course => (
          <div key={course.id} onClick={() => setSelectedCourse(course)} className="group relative flex items-center justify-between p-4 rounded-2xl border border-neutral-50/80 bg-white hover:bg-neutral-50/40 transition-all duration-300 cursor-pointer">
            <div className="flex items-start gap-4">
              <div className={`w-1 h-12 rounded-full mt-1 ${course.color}`} />
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-semibold text-[15px] text-neutral-900">{course.name}</span>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${course.colorClass}`}>{course.categoryLabel}</span>
                </div>
                <div className="flex items-center gap-3 text-xs text-neutral-400 mt-1.5">
                  <span className="flex items-center gap-1"><Calendar className="w-3 h-3" /> {course.courseTime}</span>
                  <span className="w-1 h-1 bg-neutral-200 rounded-full" />
                  <span className="flex items-center gap-1"><MapPin className="w-3 h-3" /> {course.location}</span>
                </div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-[11px] text-neutral-400">{course.instructor}</span>
              <ChevronRight className="w-4 h-4 text-neutral-300 group-hover:text-neutral-500 transition-transform group-hover:translate-x-0.5 duration-300" />
            </div>
          </div>
        )) : (
          <div className="py-12 text-center border border-dashed border-neutral-100 rounded-2xl">
            <BookOpen className="w-8 h-8 text-neutral-300 mx-auto mb-2 stroke-1" />
            <p className="text-xs text-neutral-400">当日无排课日程，去图书馆自修吧 ☕</p>
          </div>
        )}
      </div>
      {onQuickAction && (
        <div className="mt-5 pt-4 border-t border-neutral-100 flex items-center justify-between text-xs">
          <span className="text-neutral-400">对选课、考勤有疑问？</span>
          <button onClick={() => onQuickAction('帮我分析一下我今天的课程合理度，并给出备考建议。')} className="font-medium text-neutral-900 hover:text-neutral-600 transition-colors flex items-center gap-1 hover:underline">咨询智能表单 ⚡</button>
        </div>
      )}
      {selectedCourse && (
        <div className="fixed inset-0 bg-black/10 backdrop-blur-[3px] z-[999] flex items-center justify-center p-4">
          <div className="bg-white rounded-[28px] border border-neutral-100 p-8 max-w-sm w-full shadow-2xl">
            <div className="flex justify-between items-start mb-6">
              <span className={`text-[10px] px-2.5 py-1 rounded-full font-medium ${selectedCourse.colorClass}`}>{selectedCourse.category} {selectedCourse.code}</span>
              <button onClick={() => setSelectedCourse(null)} className="text-neutral-400 hover:text-neutral-700 p-1 hover:bg-neutral-50 rounded-full">✕</button>
            </div>
            <h3 className="text-lg font-bold text-neutral-900 mb-2">{selectedCourse.name}</h3>
            <div className="space-y-4 border-t border-b border-neutral-50 py-4 mb-6">
              {[{icon:User,label:'主讲讲师',val:selectedCourse.instructor},{icon:Calendar,label:'上课时间',val:`${selectedCourse.courseTime} (周${adjustedDay})`},{icon:MapPin,label:'物理位置',val:selectedCourse.location},{icon:Award,label:'毕业权重',val:`${selectedCourse.credits}.0 学分`}].map((item,i)=>(
                <div key={i} className="flex items-center gap-3 text-xs"><item.icon className="w-4 h-4 text-neutral-400" /><div><div className="font-medium text-neutral-800">{item.val}</div><div className="text-[10px] text-neutral-400">{item.label}</div></div></div>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <button onClick={() => { onQuickAction?.(`我要对课程「${selectedCourse.name}」进行课堂签到。`); setSelectedCourse(null); }} className="w-full py-2.5 bg-neutral-900 text-white rounded-xl text-xs font-semibold hover:bg-neutral-800">物理入座签到</button>
              <button onClick={() => { onQuickAction?.(`请帮我检索并提供「${selectedCourse.name}」这门课的复习大纲。`); setSelectedCourse(null); }} className="w-full py-2.5 border border-neutral-200 text-neutral-700 bg-white rounded-xl text-xs font-semibold hover:bg-neutral-50">生成课程大纲</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
