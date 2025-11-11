import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'


function App() {
  return (
    <div className="min-h-screen flex bg-background">
      {/* Sidebar */}
      <aside className="w-64 bg-primary text-white p-4">
        <h1 className="text-xl font-semibold mb-6">Tutorías</h1>
        <nav className="space-y-2">
          <button className="block w-full text-left px-3 py-2 rounded-lg bg-white/10 hover:bg-white/20">
            Dashboard
          </button>
          <button className="block w-full text-left px-3 py-2 rounded-lg hover:bg-white/10">
            List Upload
          </button>
          <button className="block w-full text-left px-3 py-2 rounded-lg hover:bg-white/10">
            Assignment
          </button>
        </nav>
      </aside>

      {/* Main content */}
      <main className="flex-1 p-6 space-y-4">
        <header className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold text-text">Dashboard</h2>
          <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
            FG
          </div>
        </header>

        <section className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-surface rounded-xl p-4 shadow-sm border border-border">
            <p className="text-sm text-gray-500">Active Students</p>
            <p className="text-2xl font-bold text-text">324</p>
          </div>
          <div className="bg-surface rounded-xl p-4 shadow-sm border border-border">
            <p className="text-sm text-gray-500">Temporary Leave</p>
            <p className="text-2xl font-bold text-text">14</p>
          </div>
          <div className="bg-surface rounded-xl p-4 shadow-sm border border-border">
            <p className="text-sm text-gray-500">Mobility</p>
            <p className="text-2xl font-bold text-text">7</p>
          </div>
          <div className="bg-surface rounded-xl p-4 shadow-sm border border-border">
            <p className="text-sm text-gray-500">Re-entries</p>
            <p className="text-2xl font-bold text-text">5</p>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
