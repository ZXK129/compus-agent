import React from 'react';
import type { PersonalOverview } from '../../../shared/types';
import { X, MapPin, BookOpen, Calendar, Users, Clock, ChevronRight } from 'lucide-react';

interface Props {
  data: PersonalOverview | null;
  isOpen: boolean;
  onClose: () => void;
  loading: boolean;
}

const WEEKDAY_LABELS = ['', '周一', '周二', '周三', '周四', '周五'];

export default function PersonalOverviewPanel({ data, isOpen, onClose, loading }: Props) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end" onClick={onClose}>
      {/* 半透明遮罩 */}
      <div className="absolute inset-0 bg-black/20 backdrop-blur-sm" />

      {/* 侧边面板 */}
      <aside
        onClick={e => e.stopPropagation()}
        className="relative w-full max-w-[480px] bg-white h-full shadow-[-20px_0_60px_rgba(0,0,0,0.08)] flex flex-col animate-[slideIn_0.25s_ease-out]"
        style={{ animation: 'slideIn 0.25s ease-out' }}
      >
        {/* 头部 */}
        <div className="p-6 border-b border-neutral-100 flex items-center justify-between shrink-0">
          <div>
            <span className="text-[10px] font-mono text-neutral-400 uppercase tracking-widest">Personal Dashboard</span>
            <h2 className="text-xl font-semibold text-neutral-900 mt-1">个人中心</h2>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-neutral-100 rounded-xl text-neutral-400 hover:text-neutral-700 transition-all">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 内容区 */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="flex gap-1.5">
                <span className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce [animation-delay:-0.3s]" />
                <span className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce [animation-delay:-0.15s]" />
                <span className="w-2 h-2 bg-neutral-400 rounded-full animate-bounce" />
              </div>
            </div>
          ) : data ? (
            <>
              {/* 学生信息卡 */}
              <div className="bg-neutral-900 text-white rounded-2xl p-5 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2" />
                <div className="relative">
                  <span className="text-[10px] font-mono text-neutral-400 uppercase tracking-widest">Student Profile</span>
                  <h3 className="text-2xl font-bold mt-1">{data.studentName}</h3>
                  <div className="flex items-center gap-3 mt-2 text-sm text-neutral-300">
                    <span>{data.studentNo}</span>
                    <span className="w-1 h-1 rounded-full bg-neutral-500" />
                    <span>{data.department}</span>
                  </div>
                  <p className="text-xs text-neutral-400 mt-2 font-mono">{data.semesterInfo}</p>
                </div>
              </div>

              {/* 今日座位 */}
              <Section title="当前座位" icon={<MapPin className="w-4 h-4" />} accent="emerald">
                {data.bookedSeat ? (
                  <div className="p-4 bg-emerald-50/50 border border-emerald-100 rounded-2xl">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-semibold text-emerald-800">
                          {data.bookedSeat.floorArea} 层 · {data.bookedSeat.seatCode}
                        </p>
                        <p className="text-[11px] text-emerald-600 mt-0.5">{data.bookedSeat.timeLabel}</p>
                      </div>
                      <div className="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center">
                        <MapPin className="w-5 h-5 text-white" />
                      </div>
                    </div>
                  </div>
                ) : (
                  <EmptyHint>暂未预约座位，前往「星空数字图书馆」选座</EmptyHint>
                )}
              </Section>

              {/* 今日课程 */}
              <Section title={data.todayLabel} icon={<Calendar className="w-4 h-4" />} accent="blue">
                {data.todayCourses.length > 0 ? (
                  <div className="space-y-2.5">
                    {data.todayCourses.map(c => (
                      <div key={c.id} className="flex items-center gap-3 p-3.5 bg-blue-50/50 border border-blue-100 rounded-xl hover:bg-blue-50 transition-all">
                        <div className="w-9 h-9 rounded-lg bg-blue-500 flex items-center justify-center text-[9px] font-bold text-white shrink-0">
                          {c.courseTime?.split(' - ')[0] || '--'}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs font-semibold text-neutral-800 line-clamp-1">{c.name}</p>
                          <p className="text-[10px] text-neutral-400 mt-0.5">{c.instructor} · {c.location}</p>
                        </div>
                        <span className="text-[9px] font-mono text-neutral-400 shrink-0">{c.credits}学分</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyHint>{data.todayLabel.includes('周末') ? data.todayLabel : '今天没有课程安排 🎉'}</EmptyHint>
                )}
              </Section>

              {/* 在借图书 */}
              <Section title={`在借图书 (${data.borrowedCount}/${data.maxBorrowLimit})`} icon={<BookOpen className="w-4 h-4" />} accent="amber">
                {data.borrowedBooks.length > 0 ? (
                  <div className="space-y-2.5">
                    {data.borrowedBooks.map(b => {
                      const overdue = b.daysRemaining <= 0;
                      return (
                        <div key={b.id} className={`flex items-center gap-3 p-3.5 rounded-xl border transition-all ${overdue ? 'bg-red-50/50 border-red-200' : 'bg-amber-50/50 border-amber-100 hover:bg-amber-50'}`}>
                          <div className={`w-9 h-12 rounded-r-md rounded-l-sm shadow-sm flex items-center justify-center text-[10px] font-bold text-white uppercase shrink-0 ${b.coverColor}`}>
                            {b.title.charAt(0)}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-xs font-semibold text-neutral-800 line-clamp-1">{b.title}</p>
                            <p className="text-[10px] text-neutral-400 mt-0.5">{b.author}</p>
                            <div className="mt-1.5 flex items-center gap-2">
                              <div className="w-14 bg-neutral-200 h-1 rounded-full">
                                <div className="bg-neutral-900 h-full rounded-full" style={{ width: `${b.progress}%` }} />
                              </div>
                              <span className="text-[9px] text-neutral-400">已读{b.progress}%</span>
                            </div>
                          </div>
                          <div className="text-right shrink-0">
                            <span className={`text-[10px] font-mono font-semibold ${overdue ? 'text-red-500' : b.daysRemaining <= 5 ? 'text-amber-600' : 'text-neutral-500'}`}>
                              {overdue ? '逾期!' : `${b.daysRemaining}天`}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <EmptyHint>暂无在借图书，去图书馆借一本好书 📖</EmptyHint>
                )}
              </Section>

              {/* 已报名活动 */}
              <Section title={`已报名活动 (${data.joinedEvents.length})`} icon={<Users className="w-4 h-4" />} accent="purple">
                {data.joinedEvents.length > 0 ? (
                  <div className="space-y-2.5">
                    {data.joinedEvents.map(e => (
                      <div key={e.id} className="flex items-center gap-3 p-3.5 bg-purple-50/50 border border-purple-100 rounded-xl hover:bg-purple-50 transition-all">
                        <div className="w-9 h-9 rounded-lg bg-purple-500 flex items-center justify-center shrink-0">
                          <Users className="w-4 h-4 text-white" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs font-semibold text-neutral-800 line-clamp-1">{e.title}</p>
                          <div className="flex items-center gap-2 mt-0.5">
                            <span className="text-[10px] text-neutral-400">{e.location}</span>
                            <span className="text-[9px] px-1.5 py-0.5 rounded-full bg-purple-100 text-purple-600 font-medium">{e.tag}</span>
                          </div>
                        </div>
                        <span className="text-[9px] text-neutral-400 font-mono shrink-0">{e.timeLabel}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyHint>暂未报名任何活动，去看看「我在校园」有什么新鲜事 🎒</EmptyHint>
                )}
              </Section>
            </>
          ) : (
            <EmptyHint>加载失败，请稍后重试</EmptyHint>
          )}
        </div>

        {/* 底部操作 */}
        <div className="p-4 border-t border-neutral-100 bg-neutral-50/50 shrink-0">
          <p className="text-[10px] text-neutral-400 text-center font-mono">
            REST API · 数据实时同步 · 校园智能体 v2.0
          </p>
        </div>
      </aside>

      {/* 动画样式注入 */}
      <style>{`
        @keyframes slideIn {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
}

/** 分区标题 */
function Section({ title, icon, accent, children }: {
  title: string;
  icon: React.ReactNode;
  accent: 'emerald' | 'blue' | 'amber' | 'purple';
  children: React.ReactNode;
}) {
  const borderMap = { emerald: 'border-emerald-200', blue: 'border-blue-200', amber: 'border-amber-200', purple: 'border-purple-200' };
  return (
    <div className={`border-l-2 ${borderMap[accent]} pl-4`}>
      <h3 className="text-xs font-semibold text-neutral-700 flex items-center gap-2 mb-3 uppercase tracking-wide">
        {icon}
        {title}
      </h3>
      {children}
    </div>
  );
}

/** 空状态提示 */
function EmptyHint({ children }: { children: string }) {
  return (
    <div className="flex items-center gap-1.5 text-[11px] text-neutral-400 py-3">
      <ChevronRight className="w-3.5 h-3.5 text-neutral-300" />
      {children}
    </div>
  );
}
