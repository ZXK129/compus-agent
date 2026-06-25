import React, { useState } from 'react';
import type { CardInfo } from '../../../shared/types';
import { CreditCard, Coffee, Truck, Printer, Sparkles } from 'lucide-react';

interface Props { balance: number; cardInfo: CardInfo | null; onTopUp: (amount: number) => void; onQuickAction?: (actionText: string) => void; }

export default function SmartCard({ balance, cardInfo, onTopUp, onQuickAction }: Props) {
  const [topUpAmount, setTopUpAmount] = useState('');
  const [showSuccess, setShowSuccess] = useState(false);

  const doTopUp = (amount: number) => { onTopUp(amount); setShowSuccess(true); setTimeout(() => setShowSuccess(false), 3000); };
  const handleCustom = (e: React.FormEvent) => { e.preventDefault(); const v = parseFloat(topUpAmount); if (!isNaN(v) && v > 0) { onTopUp(v); setTopUpAmount(''); setShowSuccess(true); setTimeout(() => setShowSuccess(false), 3000); } };

  const getIcon = (cat: string) => { switch(cat) { case 'canteen': return <Coffee className="w-3.5 h-3.5 text-neutral-600" />; case 'transit': return <Truck className="w-3.5 h-3.5 text-neutral-600" />; case 'printing': return <Printer className="w-3.5 h-3.5 text-neutral-600" />; default: return <CreditCard className="w-3.5 h-3.5 text-neutral-600" />; } };

  return (
    <div className="bg-white rounded-[24px] border border-neutral-100 p-8 shadow-[0_8px_30px_rgb(0,0,0,0.012)] transition-all hover:shadow-[0_12px_40px_rgb(0,0,0,0.018)]">
      <div className="mb-6"><span className="text-xs font-mono text-neutral-400 uppercase tracking-widest">MODULE 02 / ACCOUNT</span><h2 className="text-xl font-semibold text-neutral-900 tracking-tight mt-1 flex items-center justify-between">校园一卡通 <span className="text-[10px] bg-emerald-50 text-emerald-600 border border-emerald-100 px-2 py-0.5 rounded-full font-medium flex items-center gap-1"><span className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-ping" /> NFC 已激活</span></h2></div>
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-6 relative group overflow-hidden bg-neutral-950 text-white rounded-[18px] p-5 shadow-md flex flex-col justify-between h-[155px]">
          <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full blur-2xl" />
          <div className="flex justify-between items-start z-10"><div><p className="text-[9px] text-neutral-400 font-mono uppercase">Starry University</p><p className="text-xs font-semibold text-neutral-200 mt-1">星空大学一卡通</p></div><div className="h-6 w-8 bg-neutral-800 rounded-md border border-neutral-700/50 flex items-center justify-center text-[10px] font-bold text-neutral-400">ID</div></div>
          <div className="z-10 mt-2"><span className="text-[10px] text-neutral-400 font-mono block">WALLET CARD</span><span className="text-2xl font-mono tracking-tight font-light">¥ {balance.toFixed(2)}</span></div>
          <div className="flex justify-between items-end z-10 border-t border-white/10 pt-2.5"><div><p className="text-[9px] text-neutral-400">Holder</p><p className="text-[11px] font-medium font-sans">{cardInfo?.studentName || '—'} / {cardInfo?.studentNo || '—'}</p></div><div className="flex gap-1"><div className="w-3.5 h-3.5 rounded-full bg-white/20" /><div className="w-3.5 h-3.5 rounded-full bg-white/10 -ml-2" /></div></div>
        </div>
        <div className="lg:col-span-6 flex flex-col h-[155px] justify-between">
          <div><span className="text-[11px] text-neutral-400 font-medium block mb-2">一键充值金额 (元)</span><div className="grid grid-cols-3 gap-2">{[30,50,100].map(a=><button key={a} onClick={()=>doTopUp(a)} className="py-2 text-[11px] font-semibold border border-neutral-100 rounded-xl hover:bg-neutral-900 hover:text-white hover:border-neutral-900 transition-all">+{a}</button>)}</div></div>
          <form onSubmit={handleCustom} className="flex gap-2"><input type="number" value={topUpAmount} onChange={e=>setTopUpAmount(e.target.value)} placeholder="自定义金额..." className="flex-1 border border-neutral-100 rounded-xl px-3 text-xs focus:ring-1 focus:ring-neutral-950 outline-none" /><button type="submit" className="px-4 py-2.5 bg-neutral-900 text-white rounded-xl text-xs font-semibold hover:bg-neutral-800">冲值</button></form>
        </div>
      </div>
      <div className="mt-6 border-t border-neutral-50 pt-5">
        <div className="flex justify-between items-center mb-4"><span className="text-[11px] font-semibold text-neutral-400 uppercase">最近消费账单</span><span className="text-[10px] text-neutral-500 font-mono">账单健康度: 优</span></div>
        <div className="space-y-3">
          {(cardInfo?.recentTransactions || []).map(tx=><div key={tx.id} className="flex items-center justify-between p-2.5 rounded-xl hover:bg-neutral-50/50"><div className="flex items-center gap-3"><div className="w-7 h-7 bg-neutral-50 border border-neutral-100 rounded-lg flex items-center justify-center">{getIcon(tx.category)}</div><div><p className="text-xs font-semibold text-neutral-800">{tx.item}</p><p className="text-[10px] text-neutral-400">{tx.timeLabel} · {tx.location}</p></div></div><span className={`text-xs font-mono font-medium ${tx.type==='income'?'text-emerald-600':'text-neutral-700'}`}>{tx.type==='income'?'+':'-'}{tx.amount.toFixed(2)}</span></div>)}
        </div>
      </div>
      {showSuccess && <div className="mt-4 p-3 bg-neutral-50 border border-neutral-100 rounded-xl text-neutral-800 flex items-center gap-2 text-xs"><Sparkles className="w-4 h-4 text-neutral-600" /><span>充值成功！</span></div>}
      {onQuickAction && <div className="mt-5 pt-4 border-t border-neutral-100 flex items-center justify-between text-xs"><span className="text-neutral-400">账单可疑？</span><button onClick={()=>onQuickAction('近期消费有点超支了，有什么节省指导方案吗。')} className="font-medium text-neutral-900 hover:text-neutral-600 flex items-center gap-1 hover:underline">联系智能分析师 📊</button></div>}
    </div>
  );
}
