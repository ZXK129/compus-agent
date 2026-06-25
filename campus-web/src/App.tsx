import React, { useState, useEffect, useCallback } from 'react';
import type { Course, CardInfo, LibraryBook, CampusMoment, AcademicProfile, ChatMessage, Seat, PersonalOverview } from './shared/types';
import ScheduleCard from './features/course/components/ScheduleCard';
import SmartCard from './features/card/components/SmartCard';
import PresenceCard from './features/presence/components/PresenceCard';
import LibraryCard from './features/library/components/LibraryCard';
import AcademicCompassCard from './features/academic/components/AcademicCompassCard';
import AiAssistantPanel from './features/chat/components/AiAssistantPanel';
import PersonalOverviewPanel from './features/overview/components/PersonalOverviewPanel';
import { courseApi } from './features/course/api/courseApi';
import { cardApi } from './features/card/api/cardApi';
import { presenceApi } from './features/presence/api/presenceApi';
import { libraryApi } from './features/library/api/libraryApi';
import { academicApi } from './features/academic/api/academicApi';
import { chatApi } from './features/chat/api/chatApi';
import { overviewApi } from './features/overview/api/overviewApi';
import { Sparkles, Heart, User } from 'lucide-react';

export default function App() {
  const [balance, setBalance] = useState(0);
  const [cardInfo, setCardInfo] = useState<CardInfo | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [books, setBooks] = useState<LibraryBook[]>([]);
  const [moments, setMoments] = useState<CampusMoment[]>([]);
  const [academicProfile, setAcademicProfile] = useState<AcademicProfile | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [isAiOpen, setIsAiOpen] = useState(true);
  const [isChatLoading, setIsChatLoading] = useState(false);
  const [loading, setLoading] = useState(true);

  // 个人总览
  const [overview, setOverview] = useState<PersonalOverview | null>(null);
  const [isOverviewOpen, setIsOverviewOpen] = useState(false);
  const [overviewLoading, setOverviewLoading] = useState(false);

  const handleOpenOverview = useCallback(async () => {
    setIsOverviewOpen(true);
    setOverviewLoading(true);
    try {
      const data = await overviewApi.getPersonal();
      setOverview(data);
    } catch (err) { console.error('加载个人总览失败:', err); }
    finally { setOverviewLoading(false); }
  }, []);

  // 页面初始化：并行加载所有数据
  useEffect(() => {
    async function load() {
      try {
        const [coursesData, cardData, momentsData, booksData, seatsData, acadData, chatHistory] =
          await Promise.all([
            courseApi.getAll(), cardApi.getInfo(), presenceApi.getMoments(),
            libraryApi.getBooks(), libraryApi.getSeats(), academicApi.getProfile(), chatApi.getHistory(),
          ]);
        setCourses(coursesData); setCardInfo(cardData); setBalance(cardData.balance);
        setMoments(momentsData); setBooks(booksData); setSeats(seatsData); setAcademicProfile(acadData);
        if (chatHistory.length > 0) { setMessages(chatHistory); }
        else { setMessages([{ id: 0, role: 'assistant', content: '👋 嗨！我是「星空大学」专属智能助理。\n\n试试输入：\n- "充值50元一卡通"\n- "续期快过期的图书"\n- "帮我报名草坪音乐节"', createdAt: new Date().toISOString() }]); }
      } catch (err) { console.error('数据加载失败:', err); }
      finally { setLoading(false); }
    }
    load();
  }, []);

  // 所有操作均为 API 调用，后端处理全部业务逻辑
  const handleTopUp = useCallback(async (amount: number) => {
    try { const c = await cardApi.topUp(amount); setCardInfo(c); setBalance(c.balance); } catch (err) { console.error(err); }
  }, []);
  const handleRenewBook = useCallback(async (id: number) => {
    try { await libraryApi.renewBook(id); setBooks(await libraryApi.getBooks()); } catch (err) { console.error(err); }
  }, []);
  const handleJoinEvent = useCallback(async (id: number) => {
    try { await presenceApi.joinEvent(id); setMoments(await presenceApi.getMoments()); } catch (err) { console.error(err); }
  }, []);
  const handleAddMoment = useCallback(async (title: string, tag: string, location: string) => {
    try { await presenceApi.createMoment(title, tag, location); setMoments(await presenceApi.getMoments()); } catch (err) { console.error(err); }
  }, []);
  const handleBookSeat = useCallback(async (seatId: number) => {
    try { await libraryApi.bookSeat(seatId); setSeats(await libraryApi.getSeats()); } catch (err) { console.error(err); }
  }, []);
  const handleReleaseSeat = useCallback(async () => {
    const occ = seats.find(s => s.status === 'occupied');
    if (occ) { try { await libraryApi.releaseSeat(occ.id); setSeats(await libraryApi.getSeats()); } catch (err) { console.error(err); } }
  }, [seats]);

  const handleSendMessage = useCallback(async (text: string) => {
    if (!text.trim()) return;
    setMessages(prev => [...prev, { id: Date.now(), role: 'user', content: text, createdAt: new Date().toISOString() }]);
    setIsChatLoading(true);
    try {
      const response = await chatApi.sendMessage(text);
      setMessages(response.messages);
      // 刷新关联数据（含座位、课程）
      const [cardData, momentsData, booksData, seatsData, coursesData] = await Promise.all([
        cardApi.getInfo(), presenceApi.getMoments(), libraryApi.getBooks(),
        libraryApi.getSeats(), courseApi.getAll(),
      ]);
      setCardInfo(cardData); setBalance(cardData.balance);
      setMoments(momentsData); setBooks(booksData);
      setSeats(seatsData); setCourses(coursesData);
    } catch (err) {
      setMessages(prev => [...prev, { id: Date.now(), role: 'assistant', content: '⚙️ AI 服务暂时不可达，请稍后重试。', createdAt: new Date().toISOString() }]);
    } finally { setIsChatLoading(false); }
  }, []);

  const handleClearHistory = useCallback(async () => {
    try { await chatApi.clearHistory(); setMessages(await chatApi.getHistory()); } catch (err) { console.error(err); }
  }, []);

  const triggerAgentPrompt = useCallback((promptText: string) => { setIsAiOpen(true); handleSendMessage(promptText); }, [handleSendMessage]);

  if (loading) return <div className="min-h-screen bg-white flex items-center justify-center"><div className="flex flex-col items-center gap-4"><Sparkles className="w-8 h-8 text-neutral-400 animate-pulse" /><p className="text-xs text-neutral-400 font-mono">正在连接星空大学智能网络...</p></div></div>;

  return (
    <div id="school-agent-root" className="h-screen bg-white text-neutral-900 font-sans flex overflow-hidden">
      <main className="flex-1 bg-white overflow-y-auto relative scroll-smooth">
        <header className="max-w-4xl mx-auto px-8 pt-12 pb-8 flex flex-col sm:flex-row sm:items-center justify-between border-b border-neutral-100/60 gap-4">
          <div>
            <div className="flex items-center gap-2"><span className="text-[10px] font-mono tracking-widest text-neutral-400 bg-neutral-50 border border-neutral-100 px-2.5 py-0.5 rounded-full uppercase">STARRY PORTAL</span><span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" /></div>
            <h1 className="text-3xl font-semibold tracking-tight text-neutral-950 mt-2">星空大学智能网络</h1>
            <p className="text-xs text-neutral-400 mt-1">Spring Boot + React 前后端分离 · 按功能模块化架构</p>
          </div>
          <div className="flex items-center gap-6"><div className="text-right"><span className="text-[10px] uppercase font-mono text-neutral-400 block">Current Week</span><span className="text-xs font-semibold text-neutral-700">第 16 教学周 / 春季学期</span></div><div className="h-8 w-px bg-neutral-100" /><button onClick={handleOpenOverview} className="flex items-center gap-2 px-3 py-1.5 rounded-xl hover:bg-neutral-50 transition-all border border-transparent hover:border-neutral-100" title="查看个人总览"><User className="w-4 h-4 text-neutral-500" /><span className="text-xs font-semibold text-neutral-700">{cardInfo?.studentName || '赵肖凯'}</span></button></div>
        </header>
        <div className="max-w-4xl mx-auto px-8 py-10 space-y-12 pb-32">
          <ScheduleCard courses={courses} onQuickAction={triggerAgentPrompt} />
          <SmartCard balance={balance} cardInfo={cardInfo} onTopUp={handleTopUp} onQuickAction={triggerAgentPrompt} />
          <PresenceCard moments={moments} onAddMoment={handleAddMoment} onJoinEvent={handleJoinEvent} onQuickAction={triggerAgentPrompt} />
          <LibraryCard books={books} seats={seats} onRenewBook={handleRenewBook} onBookSeat={handleBookSeat} onReleaseSeat={handleReleaseSeat} onQuickAction={triggerAgentPrompt} />
          <AcademicCompassCard profile={academicProfile} onQuickAction={triggerAgentPrompt} />
          <footer className="pt-8 border-t border-neutral-100/60 flex flex-col sm:flex-row items-center justify-between text-[11px] text-neutral-400 gap-4"><div className="flex items-center gap-1"><span>© 2026 Starry Digital Campus.</span><Heart className="w-3 h-3 fill-neutral-200 stroke-none" /><span>Spring Boot + React</span></div><div className="flex gap-4 font-mono"><a href="#" className="hover:text-neutral-700 underline">安全守则</a><a href="#" className="hover:text-neutral-700 underline">API规约</a></div></footer>
        </div>
      </main>
      <AiAssistantPanel messages={messages} onSendMessage={handleSendMessage} isLoading={isChatLoading} isOpen={isAiOpen} onToggleOpen={() => setIsAiOpen(!isAiOpen)} onClearHistory={handleClearHistory} />
      <PersonalOverviewPanel data={overview} isOpen={isOverviewOpen} onClose={() => setIsOverviewOpen(false)} loading={overviewLoading} />
    </div>
  );
}
