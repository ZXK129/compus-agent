import React, { useState, useRef, useEffect } from 'react';
import type { ChatMessage } from '../../../shared/types';
import { Sparkles, Send, Minimize2, CornerDownLeft } from 'lucide-react';

interface Props { messages: ChatMessage[]; onSendMessage: (text: string) => void; isLoading: boolean; isOpen: boolean; onToggleOpen: () => void; onClearHistory?: () => void; }

export default function AiAssistantPanel({ messages, onSendMessage, isLoading, isOpen, onToggleOpen, onClearHistory }: Props) {
  const [input, setInput] = useState('');
  const endRef = useRef<HTMLDivElement>(null);
  const cmds = [{text:'🏫 推荐一下今天下午的空闲自修室'},{text:'📅 我今天有哪些重点课程？'},{text:'💳 一卡通没钱了，如何在线充值'},{text:'📚 怎么续期快到期的图书'}];

  const send = (e: React.FormEvent) => { e.preventDefault(); if (input.trim() && !isLoading) { onSendMessage(input); setInput(''); } };
  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages, isLoading]);

  if (!isOpen) return <button onClick={onToggleOpen} className="fixed bottom-8 right-8 z-50 w-14 h-14 bg-neutral-950 text-white rounded-full flex items-center justify-center shadow-xl hover:bg-neutral-800 transition-all hover:scale-110 group" title="唤醒AI助理"><Sparkles className="w-6 h-6 text-white group-hover:rotate-12 transition-transform" /><span className="absolute -top-1 -right-1 bg-red-500 text-[9px] text-white px-2 py-0.5 rounded-full font-bold">AI</span></button>;

  return (
    <aside className="w-full lg:w-[420px] bg-[#F9FAFB] border-l border-neutral-100/80 flex flex-col h-full shrink-0 z-40 shadow-[-10px_0_30px_rgba(0,0,0,0.01)]">
      <div className="p-6 border-b border-neutral-100 flex items-center justify-between bg-white">
        <div className="flex items-center gap-3"><div className="w-10 h-10 rounded-2xl bg-neutral-950 flex items-center justify-center shadow-sm"><Sparkles className="w-5 h-5 text-white animate-pulse" /><span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-400 border-2 border-white rounded-full" /></div><div><h3 className="text-sm font-semibold text-neutral-900">星空校园 AI 智能体</h3><span className="text-[10px] text-neutral-450 flex items-center gap-1 font-mono"><span className="inline-block w-1 h-1 rounded-full bg-emerald-500" /> ONLINE</span></div></div>
        <div className="flex items-center gap-2">{onClearHistory && messages.length>1 && <button onClick={onClearHistory} className="p-1 px-2 hover:bg-neutral-100 rounded-lg text-[10px] text-neutral-400 hover:text-neutral-700">清空</button>}<button onClick={onToggleOpen} className="p-2 hover:bg-neutral-100 rounded-xl text-neutral-400 hover:text-neutral-700"><Minimize2 className="w-4 h-4" /></button></div>
      </div>
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {messages.map(m=>{const isAI=m.role==='assistant';return <div key={m.id} className={`flex gap-3.5 max-w-[88%] ${isAI?'mr-auto':'ml-auto flex-row-reverse'}`}><div className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-mono font-bold border ${isAI?'bg-neutral-900 text-white border-neutral-900':'bg-white text-neutral-700 border-neutral-200/50'}`}>{isAI?'AI':'ME'}</div><div className="space-y-1"><div className={`p-4 rounded-[20px] text-[13px] leading-relaxed ${isAI?'bg-white border border-neutral-100/60 rounded-tl-sm':'bg-neutral-900 text-neutral-50 rounded-tr-sm'}`}><div className="whitespace-pre-line">{m.content}</div></div><span className={`text-[9px] text-neutral-400 font-mono block ${isAI?'text-left':'text-right'}`}>{new Date(m.createdAt).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'})}</span></div></div>})}
        {isLoading && <div className="flex gap-3.5 max-w-[80%] mr-auto"><div className="w-7 h-7 rounded-lg bg-neutral-900 text-white flex items-center justify-center text-xs font-bold">AI</div><div className="p-4 rounded-[20px] rounded-tl-sm bg-white border border-neutral-100/60 text-xs text-neutral-400 flex items-center gap-2"><span className="flex gap-1"><span className="w-1.5 h-1.5 bg-neutral-600 rounded-full animate-bounce [animation-delay:-0.3s]"/><span className="w-1.5 h-1.5 bg-neutral-600 rounded-full animate-bounce [animation-delay:-0.15s]"/><span className="w-1.5 h-1.5 bg-neutral-600 rounded-full animate-bounce"/></span>思考计算中...</div></div>}
        <div ref={endRef}/>
      </div>
      <div className="p-4 border-t border-b border-neutral-100 bg-white/60"><span className="text-[10px] uppercase font-mono text-neutral-400 block mb-2.5 pl-1.5">快捷学术指令</span><div className="flex flex-wrap gap-2">{cmds.map((c,i)=><button key={i} onClick={()=>onSendMessage(c.text)} className="text-[11px] bg-white hover:bg-neutral-900 hover:text-white border border-neutral-200/50 transition-all px-3 py-1.5 rounded-full font-medium text-neutral-700">{c.text}</button>)}</div></div>
      <div className="p-6 bg-white border-t"><form onSubmit={send} className="relative flex items-center"><input type="text" value={input} onChange={e=>setInput(e.target.value)} disabled={isLoading} placeholder="输入需求，如 '充值一卡通20元'..." className="w-full bg-neutral-50 border border-neutral-100 rounded-2xl py-3.5 pl-4 pr-16 text-xs outline-none focus:ring-1 focus:ring-neutral-950" /><div className="absolute right-2"><button type="submit" disabled={!input.trim()||isLoading} className={`w-9 h-9 rounded-xl flex items-center justify-center ${input.trim()&&!isLoading?'bg-neutral-950 text-white':'bg-neutral-100 text-neutral-400'}`}><Send className="w-4 h-4" /></button></div></form><div className="mt-3 flex items-center justify-between text-[10px] text-neutral-400 px-1 font-mono"><span><CornerDownLeft className="w-3 h-3 inline" /> Enter 发送</span><span>Spring Boot · REST v2.0</span></div></div>
    </aside>
  );
}
