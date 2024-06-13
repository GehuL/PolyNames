function onLoad()
{
    const cards = document.getElementsByClassName("cards")[0];

    for(let i = 0; i < 25; i++)
    {
        const card = document.createElement("div");
        card.classList.add("card");
        card.innerHTML = "TEST";
        cards.appendChild(card);
    }

    const select_nbr = document.getElementById("nombre_indice");
    for(let i = 1; i < 10; i++)
    {
        const option = document.createElement("option");
        option.text = i;
        select_nbr.add(option);
    }
}

window.addEventListener("load", onLoad);

// deroule de la game 


