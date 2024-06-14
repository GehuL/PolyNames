export class CardsView
{
    constructor()
    {

    }

    #displayCards(cardslist)
    {
        const cards = document.getElementsByClassName("cards")[0];

        for(let i = 0; i < 25; i++)//enlever la boucle quand on fera le traitement pour recuperer les cartes depuis le serveur
        {
            const card = document.createElement("div");
            card.setAttribute("id", 1);
            card.dataset.color = "GRIS";
            card.classList.add("card");
            card.innerHTML = "TEST";
            cards.appendChild(card);
        }
    }
    
}

