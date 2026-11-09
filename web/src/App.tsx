import { useState } from "react";
import "./App.css";

import { Outlet } from "react-router";
import { NavLink } from "react-router-dom";

function App() {
  const [menuOpen, setMenuOpen] = useState(false);

  function handleNavClick() {
    setMenuOpen(false);
  }

  const navLinkClass = ({ isActive }: { isActive: boolean }) => (isActive ? "navLink navLink--active" : "navLink");

  return (
    <div className="layoutGrid">
      <div className="header">
        <button
          type="button"
          className="menuToggle"
          aria-label={menuOpen ? "Close menu" : "Open menu"}
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((o) => !o)}
        >
          {menuOpen ? "✕" : "☰"} Menu
        </button>
        <h1>Steam Playtime Analyzer</h1>
      </div>
      <div className={`nav${menuOpen ? " nav--open" : ""}`}>
        <ul>
          <li>
            <NavLink to="/" end className={navLinkClass} onClick={handleNavClick}>
              <span className="navIcon">🏠</span> Home
            </NavLink>
          </li>
          <li className="navGroupLabel">Playtime</li>
          <li className="navGroupItem">
            <NavLink to="/playtime-by-host-and-year" className={navLinkClass} onClick={handleNavClick}>
              By host &amp; year
            </NavLink>
          </li>
          <li className="navGroupItem">
            <NavLink to="/playtime-by-host-and-month" className={navLinkClass} onClick={handleNavClick}>
              By host &amp; month
            </NavLink>
          </li>
          <li className="navGroupItem">
            <NavLink to="/playtime-by-year-and-month" className={navLinkClass} onClick={handleNavClick}>
              By year &amp; month
            </NavLink>
          </li>
          <li className="navGroupItem">
            <NavLink to="/playtime-by-host" className={navLinkClass} onClick={handleNavClick}>
              By host
            </NavLink>
          </li>
          <li className="navDivider" />
          <li>
            <NavLink to="/monthly" className={navLinkClass} onClick={handleNavClick}>
              <span className="navIcon">📅</span> Monthly stats
            </NavLink>
          </li>
          <li>
            <NavLink to="/game" className={navLinkClass} onClick={handleNavClick}>
              <span className="navIcon">🎮</span> Game stats
            </NavLink>
          </li>
          <li>
            <NavLink to="/calendar" className={navLinkClass} onClick={handleNavClick}>
              <span className="navIcon">🗓️</span> Calendar
            </NavLink>
          </li>
        </ul>
      </div>
      <div className="content">
        <Outlet />
      </div>
    </div>
  );
}

export default App;
