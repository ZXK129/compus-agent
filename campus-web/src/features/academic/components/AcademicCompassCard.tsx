import React from 'react';
import type { AcademicProfile } from '../../../shared/types';
import { Award, GraduationCap, BookCheck, Sparkles } from 'lucide-react';

interface Props { profile: AcademicProfile | null; onQuickAction?: (actionText: string) => void; }

export default function AcademicCompassCard({ profile, onQuickAction }: Props) {
  if (!profile) return <div className="bg-white rounded-[24px] border border-neutral-100 p-8"><span className="text-xs font-mono text-neutral-400 uppercase">MODULE 05 / ACHIEVEMENT</span><h2 className="text-xl font-semibold text-neutral-900 mt-1">学业成就与学术罗盘</h2><p className="text-xs text-neutral-400 py-8 text-center">加载中...</p></div>;

  return (
    <div className="bg-white rounded-[24px] border border-neutral-100 p-8 shadow-[0_8px_30px_rgb(0,0,0,0.012)] transition-all hover:shadow-[0_12px_40px_rgb(0,0,0,0.018)]">
      <div className="mb-6"><span className="text-xs font-mono text-neutral-400 uppercase tracking-widest">MODULE 05 / ACHIEVEMENT</span><h2 className="text-xl font-semibold text-neutral-900 tracking-tight mt-1 flex items-center justify-between">学业成就与学术罗盘 <GraduationCap className="w-5 h-5 text-neutral-800" /></h2></div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pb-6 border-b border-neutral-50">
        <div className="space-y-4">
          <div className="p-4 border border-neutral-100 bg-neutral-50/40 rounded-2xl flex items-center justify-between"><div><span className="text-[10px] uppercase font-mono text-neutral-400">Scholastic GPA</span><p className="text-2xl font-mono font-bold text-neutral-900 mt-1">{profile.gpa.toFixed(2)}/{profile.maxGpa.toFixed(1)}</p></div><div className="w-9 h-9 bg-neutral-900 text-white rounded-xl flex items-center justify-center"><Award className="w-4.5 h-4.5" /></div></div>
          <div className="p-4 border border-neutral-100 bg-neutral-50/40 rounded-2xl"><div className="flex justify-between items-center mb-1.5"><span className="text-[10px] uppercase font-mono text-neutral-400">学分攻读进度</span><span className="text-xs font-mono font-medium text-neutral-800">{profile.creditsEarned}/{profile.creditsRequired}</span></div><div className="w-full bg-neutral-100 h-2 rounded-full"><div className="bg-neutral-900 h-full rounded-full" style={{width:`${profile.progressPercent}%`}}/></div><span className="text-[9px] text-neutral-450 mt-1.5 block text-right font-mono">已斩获 {profile.progressPercent}%</span></div>
        </div>
        <div className="space-y-3"><span className="text-[11px] font-semibold text-neutral-400 uppercase block">学术雷达能力域</span>{profile.strengths.map((s,i)=><div key={i} className="space-y-1"><div className="flex justify-between text-[11px]"><span className="font-medium">{s.subject}</span><span className="font-mono text-neutral-450">{s.val}%</span></div><div className="w-full bg-neutral-50 h-1.5 rounded-full"><div className="bg-neutral-800 h-full rounded-full" style={{width:`${s.val}%`}}/></div></div>)}</div>
      </div>
      <div className="mt-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs"><div className="flex items-center gap-1.5 text-neutral-500"><BookCheck className="w-4 h-4 text-neutral-600" /><span>学位必修课及学分已完美匹配。</span></div>{onQuickAction && <button onClick={()=>onQuickAction('基于我当前的学业GPA和算法逻辑长板，帮我规划保研路径。')} className="px-3.5 py-2 bg-neutral-950 text-white rounded-xl text-xs font-semibold hover:bg-neutral-800 flex items-center gap-1"><Sparkles className="w-3 h-3" /><span>智能学术画像审计</span></button>}</div>
    </div>
  );
}
