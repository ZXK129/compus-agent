import React, { useState, useMemo } from 'react';
import type { LibraryBook, Seat } from '../../../shared/types';
import { Clock, CheckCircle, Search, MapPin } from 'lucide-react';

interface Props { books: LibraryBook[]; seats: Seat[]; onRenewBook: (id: number) => void; onBookSeat: (id: number) => void; onReleaseSeat: () => void; onQuickAction?: (actionText: string) => void; }

export default function LibraryCard({ books, seats, onRenewBook, onBookSeat, onReleaseSeat, onQuickAction }: Props) {
  const [query, setQuery] = useState(''); const [tab, setTab] = useState<'loans'|'seats'>('loans'); const [msg, setMsg] = useState<string|null>(null);
  const [seatFloor, setSeatFloor] = useState<string>('1F');
  const occ = seats.find(s=>s.status==='occupied');

  // 按楼层分组座位
  const seatsByFloor = useMemo(() => {
    const grouped: Record<string, Seat[]> = {};
    seats.forEach(s => {
      const floor = s.floorArea || s.seatCode?.replace(/^(\d+F).*/, '$1') || '其他';
      if (!grouped[floor]) grouped[floor] = [];
      grouped[floor].push(s);
    });
    return grouped;
  }, [seats]);

  const floors = useMemo(() => Object.keys(seatsByFloor).sort(), [seatsByFloor]);
  const currentFloorSeats = seatsByFloor[seatFloor] || [];

  const search = (e: React.FormEvent) => { e.preventDefault(); if (query.trim()) { setMsg(`🔍 已在馆藏库检索到与「${query}」相关的馆藏。`); onQuickAction?.(`请帮我检索与「${query}」有关的书籍。`); } };

  return (
    <div className="bg-white rounded-[24px] border border-neutral-100 p-8 shadow-[0_8px_30px_rgb(0,0,0,0.012)] transition-all hover:shadow-[0_12px_40px_rgb(0,0,0,0.018)]">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6"><div><span className="text-xs font-mono text-neutral-400 uppercase tracking-widest">MODULE 04 / RESOURCES</span><h2 className="text-xl font-semibold text-neutral-900 tracking-tight mt-1">星空数字图书馆</h2></div><div className="flex border border-neutral-100 rounded-xl p-1 bg-neutral-50"><button onClick={()=>setTab('loans')} className={`px-3 py-1.5 text-xs font-medium rounded-lg ${tab==='loans'?'bg-white text-neutral-900 shadow-sm':'text-neutral-400 hover:text-neutral-600'}`}>在借文献</button><button onClick={()=>setTab('seats')} className={`px-3 py-1.5 text-xs font-medium rounded-lg ${tab==='seats'?'bg-white text-neutral-900 shadow-sm':'text-neutral-400 hover:text-neutral-600'}`}>自习座位预约</button></div></div>

      <form onSubmit={search} className="relative mb-6"><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="智能检索万余册学术文献..." className="w-full bg-neutral-50 border border-neutral-100/80 rounded-2xl py-3 pl-11 pr-24 text-xs outline-none focus:ring-1 focus:ring-neutral-900 text-neutral-800" /><Search className="w-4 h-4 text-neutral-400 absolute left-4 top-3.5" /><button type="submit" className="bg-neutral-900 text-white rounded-xl px-4 py-1.5 text-xs font-semibold absolute right-2 top-2 hover:bg-neutral-800">全库检索</button></form>

      {msg && <div className="mb-5 p-3.5 bg-neutral-50 border border-neutral-100 rounded-xl text-xs"><div className="flex justify-between font-medium"><span>在线导航</span><button onClick={()=>setMsg(null)} className="text-neutral-400 font-mono">✕</button></div><p className="text-neutral-500">{msg}</p></div>}

      {tab==='loans' ? (
        <div className="space-y-4">
          <div className="flex justify-between text-[11px] font-semibold text-neutral-400 uppercase">
            <span>在借文献 ({books.length}/5)</span>
            {books.length >= 5 && <span className="text-amber-500">已达借阅上限</span>}
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {books.map(b=>{
              const isOverdue = b.daysRemaining <= 0;
              return (
                <div key={b.id} className={`p-5 border rounded-2xl bg-white hover:bg-neutral-50/20 transition-all flex items-start justify-between gap-4 ${isOverdue ? 'border-red-200 bg-red-50/30' : 'border-neutral-100'}`}>
                  <div className="flex items-start gap-3.5">
                    <div className={`w-8 h-12 rounded-r-md rounded-l-sm shadow-sm flex items-center justify-center text-[10px] font-bold text-white uppercase ${b.coverColor}`}>{b.title.charAt(0)}</div>
                    <div>
                      <h4 className="text-xs font-semibold text-neutral-900 line-clamp-1">{b.title}</h4>
                      <p className="text-[10px] text-neutral-400 mt-0.5">{b.author}</p>
                      <div className="mt-2.5 flex items-center gap-2">
                        <div className="w-16 bg-neutral-100 h-1.5 rounded-full"><div className="bg-neutral-900 h-full rounded-full" style={{width:`${b.progress}%`}}/></div>
                        <span className="text-[9px] text-neutral-400 font-mono">已读{b.progress}%</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col items-end justify-between h-12 text-right">
                    <div className="flex items-center gap-1 text-[10px]">
                      <Clock className={`w-3 h-3 ${isOverdue ? 'text-red-500' : b.daysRemaining <= 5 ? 'text-amber-500' : 'text-neutral-400'}`} />
                      <span className={`font-mono font-semibold ${isOverdue ? 'text-red-500 animate-pulse' : b.daysRemaining <= 5 ? 'text-amber-600 font-bold' : 'text-neutral-500'}`}>
                        {isOverdue ? '已逾期!' : `剩${b.daysRemaining}天`}
                      </span>
                    </div>
                    <button onClick={()=>{onRenewBook(b.id);onQuickAction?.(`请帮我续借《${b.title}》。`);}} disabled={b.renewed === 1} className={`text-[10px] font-semibold px-2 py-1 rounded-lg border transition-all ${b.renewed === 1 ? 'text-neutral-300 border-neutral-100 bg-neutral-50 cursor-not-allowed' : 'text-neutral-900 border-neutral-100 hover:bg-neutral-50'}`}>
                      {b.renewed === 1 ? '已续借' : '一键续期'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
          {books.length === 0 && <p className="text-xs text-neutral-400 text-center py-8">暂无在借文献，去检索一本好书吧 📖</p>}
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex justify-between text-[11px] font-semibold text-neutral-400 uppercase">
            <span>座位极速预约 · 8:00-21:00</span>
            {occ ? <span className="text-emerald-600 font-medium flex items-center gap-1"><CheckCircle className="w-3 h-3" />已锁定：{occ.seatCode}</span> : <span className="text-neutral-400">暂未选座</span>}
          </div>

          {/* 当前预约状态 */}
          <div className="bg-neutral-50/50 border border-neutral-100 p-4 rounded-2xl">
            {occ ? (
              <div className="flex items-center justify-between text-xs">
                <div>
                  <p className="font-semibold text-neutral-800 flex items-center gap-1.5">
                    <CheckCircle className="w-4 h-4 text-emerald-500" />
                    图书馆 {occ.floorArea} 层 · 独学空间舱 {occ.seatCode}
                  </p>
                  <p className="text-[10px] text-neutral-400 mt-1">时段: {occ.timeLabel}</p>
                </div>
                <button onClick={onReleaseSeat} className="px-3 py-1.5 bg-neutral-900 text-white rounded-lg text-[10px] font-semibold hover:bg-neutral-800 transition-all">离座签退</button>
              </div>
            ) : (
              <p className="text-xs text-neutral-400 text-center py-2 flex items-center justify-center gap-1.5">
                <MapPin className="w-3.5 h-3.5" />请在下方选择楼层后点选空余舱位
              </p>
            )}
          </div>

          {/* 楼层切换 Tab */}
          <div className="flex gap-2">
            {floors.map(floor => {
              const availableCount = seatsByFloor[floor]?.filter(s => s.status === 'available').length || 0;
              return (
                <button key={floor} onClick={() => setSeatFloor(floor)}
                  className={`flex-1 py-2.5 rounded-xl text-xs font-semibold border transition-all ${
                    seatFloor === floor
                      ? 'bg-neutral-900 text-white border-neutral-900 shadow-sm'
                      : 'bg-white text-neutral-500 border-neutral-100 hover:bg-neutral-50'
                  }`}>
                  {floor} 层
                  <span className={`block text-[9px] font-normal mt-0.5 ${seatFloor === floor ? 'text-neutral-300' : 'text-neutral-400'}`}>
                    {availableCount} 座空闲
                  </span>
                </button>
              );
            })}
          </div>

          {/* 当前楼层座位网格 */}
          <div className="bg-neutral-50/30 border border-neutral-100 rounded-2xl p-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-[10px] font-semibold text-neutral-500 uppercase font-mono">{seatFloor} 层座位分布</span>
              <div className="flex items-center gap-3 text-[9px] text-neutral-400">
                <span className="flex items-center gap-1"><span className="w-2 h-2 rounded bg-white border border-neutral-200" /> 空闲</span>
                <span className="flex items-center gap-1"><span className="w-2 h-2 rounded bg-neutral-900" /> 已预约</span>
              </div>
            </div>
            <div className="grid grid-cols-8 sm:grid-cols-12 md:grid-cols-16 gap-1.5 max-h-[280px] overflow-y-auto pr-1">
              {currentFloorSeats.map(s => (
                <button key={s.id}
                  onClick={() => s.status === 'available' && onBookSeat(s.id)}
                  disabled={s.status === 'occupied'}
                  title={s.seatCode + (s.status === 'occupied' ? ' (已占用)' : ' (可预约)')}
                  className={`py-1.5 text-[9px] font-mono font-medium rounded-lg border transition-all ${
                    s.status === 'occupied'
                      ? 'bg-neutral-900 text-white border-neutral-950 cursor-not-allowed opacity-80'
                      : 'bg-white border-neutral-100 text-neutral-600 hover:border-neutral-300 hover:bg-neutral-50 hover:shadow-sm cursor-pointer'
                  }`}>
                  {s.seatCode?.replace(/^\d+F/, '')}
                </button>
              ))}
            </div>
            {currentFloorSeats.length === 0 && (
              <p className="text-xs text-neutral-400 text-center py-6">该楼层暂无座位数据</p>
            )}
          </div>
        </div>
      )}

      {onQuickAction && (
        <div className="mt-5 pt-4 border-t border-neutral-100 flex items-center justify-between text-xs">
          <span className="text-neutral-400">想预约多媒体研讨间？</span>
          <button onClick={()=>onQuickAction('今天下午有哪几个多媒体研讨间可供5人小组使用？')} className="font-medium text-neutral-900 hover:text-neutral-600 flex items-center gap-1 hover:underline">研讨空间直连 📚</button>
        </div>
      )}
    </div>
  );
}
