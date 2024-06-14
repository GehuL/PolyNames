const baseURL = "http://localhost:8080";

export class ApiService
{ 
    constructor(partieId, playerId)
    {
        this.partieId = partieId;
        this.playerId = playerId;
    }

    async swapRole()
    {
        return await fetch(baseURL+"/role/swap/"+this.partieId,{method:"post"});;
    }

    /** Commence la partie et change de page
    * @param {*} randomly Indique si il faut démarrer la partie avec les roles aléatoires
     */
    async startGame(randomly)
    {
        let url = baseURL + "/start/";
       
        if(randomly)
            url += 'random/';

        return await fetch(url+this.partieId, {"method": "put"})
    }

    async getPlayers()
    {
        const url = baseURL + "/players/"+this.partieId;
        return await fetch(url, {"method": "get"});
    }

    // Renvoie la liste des cartes
    async getCards()
    {
        return await fetch(baseURL+"/cards/"+this.partieId+"/"+this.playerId);
    }
}