import { useRef } from "react";
import { createSearchParams, useNavigate } from "react-router-dom";
import Select from "react-select";
import { useDatabase } from "../database";
import { getGameSearchOptions } from "../services/games";

export default function GameSearch() {
  const database = useDatabase();
  const searchOptions = getGameSearchOptions(database);
  const navigate = useNavigate();
  const selectInputRef = useRef<any>();

  function onGameSelected(e: any) {
    const options = {
      pathname: "/game",
      search: createSearchParams({
        steam_app_id: e.value,
      }).toString(),
    };

    navigate(options);
    selectInputRef.current.select.clearValue();
  }

  return (
    <div style={{ width: "50%", color: "black", margin: "auto" }}>
      <Select ref={selectInputRef} options={searchOptions} onChange={onGameSelected} placeholder="Select a game" />
    </div>
  );
}
