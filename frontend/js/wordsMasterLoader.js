import { CardsView } from "./views/cards-view.js";
 
function onLoad()
{
    const view = new CardsView();

    const select_nbr = document.getElementById("nombre_indice");
    for(let i = 1; i < 10; i++)
    {
        const option = document.createElement("option");
        option.text = i;
        select_nbr.add(option);
    }
}

window.addEventListener("load", onLoad);

