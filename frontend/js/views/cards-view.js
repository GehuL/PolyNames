export class CardsView
{
    constructor()
    {
        this.#displayCards()
    }

    #displayCards(cardslist)
    {
        const cards = document.getElementsByClassName("cards")[0];

        for(let i = 0; i < 25; i++)
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